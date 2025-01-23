import com.typesafe.config.ConfigFactory
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import java.io.File
import java.nio.file.Paths

class CI(
    val localFile: String,
    configPath: String,
    result: (String) -> Unit
) {

    private val config = ConfigFactory.parseFile(File(configPath))
    private val host: String = config.getString("CI.host")
    private val port: Int = config.getInt("CI.port")
    private val user: String = config.getString("CI.user")
    private val privateKeyPatch: String = config.getString("CI.privateKey")
    private val remoteDir: String = config.getString("CI.remoteDir")

    init {
        val ssh = SSHClient()
        ssh.addHostKeyVerifier(PromiscuousVerifier())

        try {
            result("Loading private key from $privateKeyPatch")
            val keyProvider: KeyProvider = ssh.loadKeys(privateKeyPatch, null as CharArray?)

            result("Connecting to $host:$port")
            ssh.connect(host, port)

            result("Authenticating user $user")
            ssh.authPublickey(user, keyProvider)

            result("Opening SFTP client")
            val sftp = ssh.newSFTPClient()
            sftp.use { sftpClient ->
                val tmpFileName = "${Paths.get(localFile).fileName}.tmp"
                val remoteTmpFile = "$remoteDir/$tmpFileName"
                val remoteFinalFile = "$remoteDir/${Paths.get(localFile).fileName}"

                result("Uploading file: $localFile to $remoteTmpFile")
                sftpClient.put(localFile, remoteTmpFile)

                result("Renaming file: $remoteTmpFile to $remoteFinalFile")
                sftpClient.rename(remoteTmpFile, remoteFinalFile)

                result("File successfully uploaded and renamed on SFTP server")
            }
        } catch (e: Exception) {
            result("Upload file error: ${e.message}")
            e.printStackTrace()
        } finally {
            result("Disconnecting")
            ssh.disconnect()
        }
    }
}