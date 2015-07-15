package io.gameq.gameqwindows.ConnectionHandler;

import net.sourceforge.jdpapi.DataProtector;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by fabianwikstrom on 7/15/2015.
 */
public class EncryptedProperties {

        private final DataProtector protector;
        private final Properties properties;
        private final Map<String, String> keys;

        public EncryptedProperties() {
            this.protector = new DataProtector();
            this.properties = new Properties();
            this.keys = new HashMap<String, String>();
        }

        public void store(Writer writer) throws IOException {
            properties.store(writer, "");
        }

        public void load(Reader reader) throws IOException {
            properties.load(reader);

            for (String key : properties.stringPropertyNames()) {
                keys.put(decrypt(key), key);
            }
        }

        public void setProperty(String key, String value) {
            String encryptedKey = encrypt(key);
            String encryptedValue = encrypt(value);
            keys.put(key, encryptedKey);

            properties.setProperty(encryptedKey, encryptedValue);
        }

        public String getProperty(String key) {
            String encryptedKey = keys.get(key);
            String encryptedValue = properties.getProperty(encryptedKey);
            return decrypt(encryptedValue);
        }

        private String encrypt(String key) {
            byte [] data = protector.protect(key);
            return new String(Base64.getEncoder().encode(data));
        }

        private String decrypt(String encryptedString) {
            byte [] data = Base64.getDecoder().decode(encryptedString.getBytes());
            return protector.unprotect(data);
        }

        static {
            System.loadLibrary("jdpapi-native.dll");
        }
}
