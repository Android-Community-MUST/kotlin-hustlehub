package must.kdroiders.hustlehub.ui.features.auth.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        firebaseAuth = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        val provider = Provider { userRepository }
        repository = AuthRepositoryImpl(firebaseAuth, provider)
    }

    @Test
    fun `getCurrentUser returns null when no firebase user`() {
        every { firebaseAuth.currentUser } returns null
        assertNull(repository.getCurrentUser())
    }

    @Test
    fun `getCurrentUser returns user when firebase user is logged in`() {
        val mockUser = mockk<FirebaseUser>()
        every { firebaseAuth.currentUser } returns mockUser
        assertEquals(mockUser, repository.getCurrentUser())
    }

    @Test
    fun `logout signs out of firebase`() = runTest {
        repository.logout()
        verify(exactly = 1) { firebaseAuth.signOut() }
    }

    @Test
    fun `sendPasswordResetEmail calls firebaseAuth sendPasswordResetEmail`() = runTest {
        val mockTask = mockk<Task<Void>>(relaxed = true) {
            every { isSuccessful } returns true
            every { isComplete } returns true
            every { exception } returns null
        }
        every { firebaseAuth.sendPasswordResetEmail("john@students.must.ac.ke") } returns mockTask

        repository.sendPasswordResetEmail("john@students.must.ac.ke")
        verify(exactly = 1) { firebaseAuth.sendPasswordResetEmail("john@students.must.ac.ke") }
    }
}
