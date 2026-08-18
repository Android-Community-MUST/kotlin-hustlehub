package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import must.kdroiders.hustlehub.core.network.ConnectivityViewModel

@Composable
fun HustleScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable (PaddingValues) -> Unit,
) {
    val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
    val isConnected by connectivityViewModel.isConnected.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                OfflineBanner(
                    isOnline = isConnected,
                    onRetry = { connectivityViewModel.retryConnectivityCheck() },
                )
                topBar()
            }
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content,
    )
}
