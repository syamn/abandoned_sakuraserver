package syam.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encrypter {
    
    /**
     * メッセージダイジェスト：MD5
     */
    public static final String ALG_MD5 = "MD5";
    
    /**
     * メッセージダイジェスト：SHA-1
     */
    public static final String ALG_SHA1 = "SHA-1";
    
    /**
     * メッセージダイジェスト：SHA-256
     */
    public static final String ALG_SHA256 = "SHA-256";
    
    /**
     * メッセージダイジェスト：SHA-384
     */
    public static final String ALG_SHA384 = "SHA-384";
    
    /**
     * メッセージダイジェスト：SHA-512
     */
    public static final String ALG_SHA512 = "SHA-512";
    
    /**
     * ハッシュ値を返す
     * 
     * @param org
     *            計算元文字列
     * @param algorithm
     *            ハッシュアルゴリズム名(Encrypter.ALG_xxxで取得できる)
     * @return ハッシュ値
     */
    public static String getHash(String org, String algorithm) {
        // 引数・アルゴリズム指定が無い場合は計算しない
        if ((org == null) || (algorithm == null)) { return null; }
        
        // 初期化
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        
        md.reset();
        md.update(org.getBytes());
        byte[] hash = md.digest();
        
        // ハッシュを16進数文字列に変換
        StringBuffer sb = new StringBuffer();
        int cnt = hash.length;
        for (int i = 0; i < cnt; i++) {
            sb.append(Integer.toHexString((hash[i] >> 4) & 0x0F));
            sb.append(Integer.toHexString(hash[i] & 0x0F));
        }
        return sb.toString();
    }
}
