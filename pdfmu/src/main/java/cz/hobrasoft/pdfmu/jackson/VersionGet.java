package cz.hobrasoft.pdfmu.jackson;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class VersionGet extends Result {

    public VersionGet(String version) {
        this.version = version;
    }

    public String version;
}
