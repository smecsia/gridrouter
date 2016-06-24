package ru.qatools.gridrouter.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import ru.qatools.gridrouter.config.Browsers;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.StringWriter;

import static java.lang.ClassLoader.getSystemResource;
import static ru.qatools.gridrouter.utils.GridRouterRule.USER_1;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public final class QuotaUtils {

    public static final String QUOTA_FILE_PATTERN
            = getSystemResource("quota/" + USER_1 + ".xml").getPath().replace(USER_1, "%s");

    private QuotaUtils() {
    }

    public static void replacePortInQuotaFile(String user, int port) {
        replacePortInQuotaFile(user, 0, 0, port);
    }

    public static void replacePortInQuotaFile(String user, int regionNum, int hostNum, int port) {
        copyQuotaFile(user, user, regionNum, hostNum, port);
    }

    public static void copyQuotaFile(String srcUser, String dstUser, int regionNum, int hostNum, int withHubPort) {
        Browsers browsers = getQuotaFor(srcUser);
        setPort(browsers, regionNum, hostNum, withHubPort);
        writeQuotaFor(dstUser, browsers);
    }

    public static Browsers getQuotaFor(String user) {
        File quotaFile = getQuotaFile(user);
        Browsers browsersOriginal = JAXB.unmarshal(quotaFile, Browsers.class);
        return SerializationUtils.clone(browsersOriginal);
    }

    public static synchronized void writeQuotaFor(String user, Browsers browsers) {
        try {
            //workaround to write the whole file at once
            StringWriter xml = new StringWriter();
            JAXB.marshal(browsers, xml);
            final File fileToWrite = getQuotaFile(user);
            final File tmpFile = File.createTempFile(user, "xml");
            FileUtils.write(tmpFile, xml.toString());
            FileUtils.copyFile(tmpFile, fileToWrite);
            FileUtils.deleteQuietly(tmpFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File getQuotaFile(String user) {
        return new File(String.format(QUOTA_FILE_PATTERN, user));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteQuotaFile(String user) {
        getQuotaFile(user).delete();
    }

    public static void setPort(Browsers browsers, int regionNum, int hostNumber, int port) {
        browsers.getBrowsers().get(0)
                .getVersions().get(0)
                .getRegions().get(regionNum)
                .getHosts().get(hostNumber)
                .setPort(port);
    }
}
