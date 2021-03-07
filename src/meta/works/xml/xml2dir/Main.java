package meta.works.xml.xml2dir;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public
class Main
{
    private final Node node;
    private final File outputDir;

    private static final
    DocumentBuilder builder;

    static
    {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        try
        {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            throw new Error(e);
        }
    }

    public
    Main(File inputFile, File outputDir) throws IOException, SAXException
    {
        this(builder.parse(inputFile).getDocumentElement(), outputDir);
    }

    public
    Main(Node node, File outputDir) throws IOException
    {
        this.node = node;
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
        var outputDir = derivedOrSpecifiedOutputDirectory(inputName, args);
        var main = new Main(new File(inputName), outputDir);
        main.run();
    }

    public
    void run() throws IOException
    {
        debug("run(): " + node.getNodeName() + " -> " + outputDir);

        var attributes = node.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            var attribute = attributes.item(i);
            var key = attribute.getNodeName();
            var value = attribute.getNodeValue();
            writeAttributeFile(key, value);
        }

        if (hasChildElements(node))
        {
            recurseOnChildElementsOnly();
            return;
        }

        var children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            var child = children.item(i);

            var nodeType = child.getNodeType();

            if (nodeType == Node.TEXT_NODE)
            {
                var content = node.getTextContent().trim();
                printBodyFile(content);
            }
        }
    }

    private
    void printBodyFile(String bodyContent) throws IOException
    {
        var bodyFile = new File(outputDir, "body.txt");
        var out = new FileOutputStream(bodyFile);

        try
        {
            out.write(bodyContent.getBytes(StandardCharsets.UTF_8));
        }
        finally
        {
            out.close();
        }
    }

    private
    void writeAttributeFile(String key, String value) throws IOException
    {
        if (value.isEmpty())
        {
            System.err.println("ignoring empty "+key+" attribute");
            return;
        }

        System.out.println("attribute: "+key+" -> "+value);
        var attributeFile = new File(outputDir, key);
        Files.createSymbolicLink(attributeFile.toPath(), new File(value).toPath());
    }

    private
    void recurseOnChildElementsOnly() throws IOException
    {
        var children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            var child = children.item(i);
            var nodeType = child.getNodeType();

            if (nodeType == Node.ELEMENT_NODE)
            {
                var name = child.getNodeName();
                System.out.println("child node: "+name);
                var sub = new Main(child, deriveOutputDirectory(name, i));
                sub.run();
            }
        }
    }

    private
    boolean hasChildElements(Node node)
    {
        var children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++)
        {
            var child = children.item(i);

            var nodeType = child.getNodeType();

            if (nodeType == Node.ELEMENT_NODE)
            {
                return true;
            }
        }

        return false;
    }

    private
    File deriveOutputDirectory(String name, int i)
    {
        return new File(outputDir, name+"-"+i);
    }

    private
    void debug(String s)
    {
        System.err.println(s);
    }

    private static
    File derivedOrSpecifiedOutputDirectory(String inputName, String[] args)
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
