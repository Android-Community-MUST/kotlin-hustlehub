package must.kdroiders.hustlehub

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() =
        rule.collect(packageName = "must.kdroiders.hustlehub") {
            pressHome()
            startActivityAndWait()

            device.wait(Until.hasObject(By.res("must.kdroiders.hustlehub:id/home_service_grid")), 5_000L)

            device
                .findObject(By.res("must.kdroiders.hustlehub:id/service_card"))
                ?.also { it.click() }
            device.wait(Until.hasObject(By.text("Contact Provider")), 3_000L)
            device.pressBack()

            device.findObject(By.desc("Chat"))?.also { it.click() }
            device.wait(Until.hasObject(By.text("Messages")), 3_000L)

            device.findObject(By.desc("Home"))?.also { it.click() }
        }
}
