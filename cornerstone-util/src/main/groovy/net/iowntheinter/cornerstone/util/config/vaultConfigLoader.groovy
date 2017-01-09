package net.iowntheinter.cornerstone.util.config

import com.bettercloud.vault.Vault
import com.bettercloud.vault.VaultConfig
import io.vertx.core.Vertx
import net.iowntheinter.cornerstone.util.configSource

/**
 * Created by g on 10/15/16.
 */
class vaultConfigLoader implements configSource {
    Vertx v
    private VAULT_ADDR = System.getenv("VAULT_ENDPOINT")
    private VAULT_TOKEN = System.getenv("VAULT_TOKEN")
    private VAULT_APPID = System.getenv("VAULT_APPID")
    final VaultConfig vault_config
    final Vault vault
    vaultConfigLoader(Vertx v) {
        this.v = v
        vault_config =
                new VaultConfig().
                        address(VAULT_ADDR)   // Defaults to "VAULT_ADDR" environment variable
                        .token(VAULT_TOKEN)   // Defaults to "VAULT_TOKEN" environment variable
                        .openTimeout(5)       // Defaults to "VAULT_OPEN_TIMEOUT" environment variable
                        .readTimeout(30)      // Defaults to "VAULT_READ_TIMEOUT" environment variable     //    See also: "sslPemUTF8()" and "sslPemResource()"
                        .sslVerify(false)     // Defaults to "VAULT_SSL_VERIFY" environment variable
                        .build();
        vault = new Vault(vault_config);
    }

    @Override
    void loadConfig(String url, cb) {

        final String value = vault.logical().read(url)
        cb([result:vault, error:null])
    }
}
