package meta.works.xml.xml2dir;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public
class Main
{
    private final Element element;
    private final File outputDir;

    public
    Main(File inputFile, File outputDir) throws ParserConfigurationException, IOException, SAXException
    {
        this(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile).getDocumentElement(), outputDir);
    }

    public
    Main(Element element, File outputDir) throws IOException
    {
        this.element = element;
        this.outputDir = outputDir;

        if (!outputDir.isDirectory() && !outputDir.mkdir())
        {
            throw new IOException("cannot create output directory: "+outputDir);
        }
    }

    public static
    void main(String[] args) throws Throwable
    {
        if (args.length < 1 || args.length > 2)
        {
            System.err.println("usage: xml2dir input.xml [output.d]");
            System.exit(1);
        }

        var inputName = args[0];
        var outputDir = derivedOrSpecified(inputName, args);
        var main = new Main(new File(inputName), outputDir);
        main.run();
    }

    public
    void run()
    {

    }

    private static
    File derivedOrSpecified(String inputName, String[] args)
    {
        if (args.length < 2)
        {
            return new File(inputName + ".d");
        }
        else
        {
            return new File(args[1]);
        }
    }
}
