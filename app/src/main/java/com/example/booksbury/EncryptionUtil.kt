
import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

// Класс, шифрующий пароль пользователя
class EncryptionUtil {

    private val ALGORITHM = "AES"
    private val KEY = "MySuperSecretKey".toByteArray() // 16 байт для AES-128

    fun encrypt(plainText: String): String {
        val secretKey: Key = SecretKeySpec(KEY, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }
}