package ib.infobot

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.File
import java.nio.file.Paths
import com.typesafe.config.ConfigFactory

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
                println("Uploading file: $localFile to $remoteDir")
                sftpClient.put(localFile, "$remoteDir/${Paths.get(localFile).fileName}")
                result("File successfully uploaded to SFTP server")
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

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -jar ModuleCI-0.1.0.jar <localFile> <config>")
        return
    }

    val localFile = args[0]
    val configPath = args[1]
    CI(localFile, configPath, result = { message -> println(message) })
}