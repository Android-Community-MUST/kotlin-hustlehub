package must.kdroiders.hustlehub.core.network

import android.content.Context
import android.net.ConnectivityManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test

class NetworkMonitorTest {

    @Test
    fun `network monitor instantiation does not throw exception`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockConnectivityManager = mockk<ConnectivityManager>(relaxed = true)
        every { mockContext.getSystemService(ConnectivityManager::class.java) } returns mockConnectivityManager

        val monitor = NetworkMonitor(mockContext)
        assertNotNull(monitor.isOnline)
    }
}
