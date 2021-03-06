/*
 * Created on 22-Feb-2005
 */
package ca.uhn.hl7v2.app.tests;

import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.frameworks;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.logProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanDir;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.parser.PipeParser;


/**
 * Unit tests for Receiver.  
 * 
 * @author <a href="mailto:bryan.tripp@uhn.on.ca">Bryan Tripp</a>
 * @version $Revision: 1.2 $ updated on $Date: 2009-09-15 12:11:19 $ by $Author: jamesagnew $
 * @author Niranjan Sharma niranjan.sharma@med.ge.com This testcase has been
 *         extended for OSGI environment using Junit4 and PAX-Exam.
 * 
 * 
 */
@RunWith(JUnit4TestRunner.class)
public class ReceiverTest {

    // you get that because you "installed" a log profile in configuration.
    public Log logger = LogFactory.getLog(ReceiverTest.class);
    
    @Inject
    BundleContext bundleContext;
    
    @Configuration
    public static Option[] configure() {
	return options(frameworks(equinox(), felix(), knopflerfish())
		, logProfile()
		, systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO")
		, mavenBundle().groupId("org.ops4j.pax.url").artifactId("pax-url-mvn").version("0.4.0")
		, wrappedBundle(mavenBundle().groupId("org.ops4j.base").artifactId("ops4j-base-util").version("0.5.3"))
		, mavenBundle().groupId("ca.uhn.hapi").artifactId("hapi-osgi-base").version("1.0-beta1")
//		, vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5006" )


	);
    } 
    
    @Test
    public void testCloseConnectionOnNullMessage() throws LLPException, IOException, InterruptedException {
        int port = 9873;
        final ServerSocket ss = new ServerSocket(port);
        Runnable acceptor = new Runnable() {
            public void run() {
                try {
                    Socket s = ss.accept();
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread acceptorThread = new Thread(acceptor);
        acceptorThread.start();
        
        Socket clientSocket = new Socket("localhost", port);
        final Properties flagHolder = new Properties();
        flagHolder.setProperty("closed", "no");
        Connection conn = new Connection(new PipeParser(), new MinLowerLayerProtocol(), clientSocket) {
            public void close() {
                super.close();
                flagHolder.setProperty("closed", "yes");
            }
        };
        
        Thread.sleep(200);
        
        assertEquals("yes", flagHolder.getProperty("closed"));
        ss.close();
    }

    @Test
    public void testCloseConnectionOnNullMessage2() throws LLPException, IOException, InterruptedException {
        //this time the client closes the socket
        int port = 9873;
        final ServerSocket ss = new ServerSocket(port);
        Runnable acceptor = new Runnable() {
            public void run() {
                try {
                    Socket s = ss.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread acceptorThread = new Thread(acceptor);
        acceptorThread.start();
        
        Socket clientSocket = new Socket("localhost", port);
        final Properties flagHolder = new Properties();
        flagHolder.setProperty("closed", "no");
        Connection conn = new Connection(new PipeParser(), new MinLowerLayerProtocol(), clientSocket) {
            public void close() {
                super.close();
                flagHolder.setProperty("closed", "yes");
            }
        };

        clientSocket.close();
        
        Thread.sleep(200);
        
        assertEquals("yes", flagHolder.getProperty("closed"));
        ss.close();
    }
}
