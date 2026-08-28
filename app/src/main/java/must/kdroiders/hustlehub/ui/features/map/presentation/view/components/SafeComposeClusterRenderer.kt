package must.kdroiders.hustlehub.ui.features.map.presentation.view.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import timber.log.Timber

class SafeComposeClusterRenderer<T : ClusterItem>(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<T>,
    private val lifecycleOwner: LifecycleOwner,
    private val savedStateRegistryOwner: SavedStateRegistryOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val clusterContent: (@Composable (Cluster<T>) -> Unit)?,
    private val clusterItemContent: (@Composable (T) -> Unit)?,
) : DefaultClusterRenderer<T>(context, map, clusterManager) {

    override fun onBeforeClusterRendered(cluster: Cluster<T>, options: MarkerOptions) {
        if (clusterContent != null) {
            val descriptor = renderComposableToBitmapDescriptor { clusterContent.invoke(cluster) }
            if (descriptor != null) {
                options.icon(descriptor)
                return
            }
        }
        super.onBeforeClusterRendered(cluster, options)
    }

    override fun onBeforeClusterItemRendered(item: T, options: MarkerOptions) {
        if (clusterItemContent != null) {
            val descriptor = renderComposableToBitmapDescriptor { clusterItemContent.invoke(item) }
            if (descriptor != null) {
                options.icon(descriptor)
                return
            }
        }
        super.onBeforeClusterItemRendered(item, options)
    }

    private fun renderComposableToBitmapDescriptor(content: @Composable () -> Unit): BitmapDescriptor? {
        return try {
            val composeView = ComposeView(context).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                setViewTreeViewModelStoreOwner(viewModelStoreOwner)
                setContent(content)
            }

            composeView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )
            val measuredWidth = composeView.measuredWidth
            val measuredHeight = composeView.measuredHeight

            if (measuredWidth <= 0 || measuredHeight <= 0) return null

            composeView.layout(0, 0, measuredWidth, measuredHeight)
            val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            composeView.draw(canvas)

            BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (e: Exception) {
            Timber.e(e, "Error rendering custom cluster marker composable")
            null
        }
    }
}
