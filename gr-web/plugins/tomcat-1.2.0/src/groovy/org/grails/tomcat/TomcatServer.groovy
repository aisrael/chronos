package org.grails.tomcat

import grails.web.container.*
import grails.util.BuildSettingsHolder
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.apache.catalina.*
import org.apache.catalina.startup.*
import org.apache.catalina.core.StandardContext
import java.beans.PropertyChangeListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.connector.Connector
import org.apache.catalina.*
import grails.util.*
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils as GPU
import org.apache.catalina.deploy.ContextEnvironment
import org.apache.catalina.deploy.ContextResource
import sun.security.tools.KeyTool
import org.apache.naming.resources.DirContextURLStreamHandlerFactory
import org.apache.naming.resources.DirContextURLStreamHandler

class TomcatServer implements EmbeddableServer {

	Tomcat tomcat
	StandardContext context
	def eventListener
	def grailsConfig
	
    protected String keystore
    protected File keystoreFile
    protected String keyPassword
	protected buildSettings
	
	TomcatServer(String basedir, String webXml, String contextPath, ClassLoader classLoader) {
		tomcat = new Tomcat()
        this.buildSettings = BuildSettingsHolder.getSettings()	

		if(contextPath=='/') contextPath = ''

		def tomcatDir = new File("${buildSettings.projectWorkDir}/tomcat").absolutePath
		def ant = new AntBuilder()		
		ant.delete(dir:tomcatDir, failonerror:false)		
		
		tomcat.basedir = tomcatDir
		context = tomcat.addWebapp(contextPath, basedir)
		tomcat.enableNaming()		
		
		// we handle reloading manually
		context.reloadable = false
		context.setAltDDName("${buildSettings.projectWorkDir}/resources/web.xml")

		def aliases = []
		def pluginManager = PluginManagerHolder.getPluginManager()
		def pluginSettings = GPU.getPluginBuildSettings()
		if(pluginManager!=null) {
			for(plugin in pluginManager.userPlugins) {
				  def dir = pluginSettings.getPluginDirForName(GrailsNameUtils.getScriptName(plugin.name))
				  def webappDir = dir ? new File("${dir.file.absolutePath}/web-app") : null
				  if (webappDir?.exists())
				        aliases << "/plugins/${plugin.fileSystemName}=${webappDir.absolutePath}"
				
			}
		}

		if(aliases) {
			context.setAliases(aliases.join(','))
		}
		TomcatLoader loader = new TomcatLoader(classLoader)
		
		loader.container = context
		context.loader = loader
		
		initialize()
	}
	
	TomcatServer(String warPath, String contextPath) {
        this.buildSettings = BuildSettingsHolder.getSettings()
		def workDir = buildSettings.projectWorkDir
		def ant = new AntBuilder()
		def tomcatDir = new File("${workDir}/tomcat").absolutePath
		def warDir = new File("${workDir}/war").absolutePath
		ant.delete(dir:tomcatDir, failonerror:false)		
		ant.delete(dir:warDir, failonerror:false)				
		ant.unzip(src:warPath, dest:warDir)
		tomcat = new Tomcat()
		if(contextPath=='/') contextPath = ''

		tomcat.basedir = tomcatDir
		
		context = tomcat.addWebapp(contextPath, warDir)						
		context.setParentClassLoader(getClass().classLoader.rootLoader)				
		tomcat.enableNaming()					

		initialize()
	}
	
    protected initialize() {


        keystore = "${buildSettings.grailsWorkDir}/ssl/keystore"
        keystoreFile = new File("${keystore}")
        keyPassword = "123456"

        System.setProperty('org.mortbay.xml.XmlParser.NotValidating', 'true')
    }
	
    /**
     * Starts the container on the default port
     */
    void start() {
		start(null, 8080)
	}

    /**
     * Starts the container on the given port
     * @param port The port number
     */
    void start(int port) {
		start(null, port)
	}

    /**
     * Starts the container on the given port
     * @param host The host to start on
     * @param port The port number
     */
    void start(String host, int port) {
		preStart()
		tomcat.port = port
		if(host) {
			tomcat.connector.setAttribute("address", host)
		}
			
		tomcat.connector.URIEncoding = 'UTF-8'
		tomcat.start()
	}
	
	private preStart() {
        eventListener?.event("ConfigureTomcat", [tomcat])	
		def jndiEntries = grailsConfig?.grails?.naming?.entries
		
		if(jndiEntries instanceof Map) {
			jndiEntries.each { key, value ->
				if(value) {
					def res = new ContextResource()
					if(value instanceof javax.sql.DataSource) {
						res.type = "javax.sql.DataSource"
					}
					else {
						res.type = value.class.name						
					}
					res.name = key
					context.namingResources.addResource res					
				}				
			}			
		}
		
	}

    /**
     * Starts a secure container running over HTTPS
     */
    void startSecure() {
		startSecure(8443)
	}

    /**
     * Starts a secure container running over HTTPS for the given port
     * @param port The port
     */
    void startSecure(int port) {
		startSecure("localhost", 8080, port)
	}
	
    /**
     * Starts a secure container running over HTTPS for the given port and host.
     * @param host The server host
     * @param httpPort The port for HTTP traffic.
     * @param httpsPort The port for HTTPS traffic.
     */
    void startSecure(String host, int httpPort, int httpsPort) {	
		preStart()
		tomcat.hostname = host
		tomcat.port = httpPort
		tomcat.connector.URIEncoding = 'UTF-8'
        if (!(keystoreFile.exists())) {
            createSSLCertificate()
        }
		
		def sslConnector = new Connector()
		sslConnector.scheme = "https"
		sslConnector.secure = true
		sslConnector.port = httpsPort
		sslConnector.setProperty("SSLEnabled","true")
		sslConnector.setAttribute("keystore", keystore)
		sslConnector.setAttribute("keystorePass", keyPassword)
		sslConnector.URIEncoding = 'UTF-8'
		tomcat.service.addConnector sslConnector
		tomcat.start()
	}
	
    /**
     * Creates the necessary SSL certificate for running in HTTPS mode
     */
    protected createSSLCertificate() {
        println 'Creating SSL Certificate...'
        if (!keystoreFile.parentFile.exists() &&
                !keystoreFile.parentFile.mkdir()) {
            def msg = "Unable to create keystore folder: " + keystoreFile.parentFile.canonicalPath
            throw new RuntimeException(msg)
        }
        String[] keytoolArgs = ["-genkey", "-alias", "localhost", "-dname",
                "CN=localhost,OU=Test,O=Test,C=US", "-keyalg", "RSA",
                "-validity", "365", "-storepass", "key", "-keystore",
                "${keystore}", "-storepass", "${keyPassword}",
                "-keypass", "${keyPassword}"]
        KeyTool.main(keytoolArgs)
        println 'Created SSL Certificate.'
    }	


    /**
     * Stops the container
     */
    void stop() {
		tomcat.stop()
	}

    /**
     * Typically combines the stop() and start() methods in order to restart the container
     */
    void restart() {
		stop()
		start()
	}

}


class TomcatLoader implements Loader, Lifecycle {

    private static Log log = LogFactory.getLog(TomcatLoader.class.getName())

    private static boolean first = true;
    
    ClassLoader classLoader
    Container container
    boolean delegate
    boolean reloadable

	TomcatLoader(ClassLoader classLoader) {
		this.classLoader = classLoader
	}

    void addPropertyChangeListener(PropertyChangeListener listener) {}

    void addRepository(String repository) {
          log.warn "Call to addRepository($repository) was ignored."
    }

    void backgroundProcess() {}

    String[] findRepositories() {
          log.warn "Call to findRepositories() returned null." 
    }

    String getInfo() { "MyLoader/1.0" }

    boolean modified() { false }

    void removePropertyChangeListener(PropertyChangeListener listener) {}

    void addLifecycleListener(LifecycleListener listener) {
        log.warn "Call to addLifecycleListener($listener) was ignored."
    }

    LifecycleListener[] findLifecycleListeners() {
        log.warn "Call to findLifecycleListeners() returned null."
    }

    void removeLifecycleListener(LifecycleListener listener) {
        log.warn "Call to removeLifecycleListener(${listener}) was ignored."	
    }

    void start()  {

      URLStreamHandlerFactory streamHandlerFactory =
	 	 	            new DirContextURLStreamHandlerFactory();

      if (first) {
          first = false;
          try {
              URL.setURLStreamHandlerFactory(streamHandlerFactory);
          } catch (Exception e) {
              // Log and continue anyway, this is not critical
              log.error("Error registering jndi stream handler", e);
          } catch (Throwable t) {
              // This is likely a dual registration
              log.info("Dual registration of jndi stream handler: "
                       + t.getMessage());
          }
      }

       DirContextURLStreamHandler.bind(classLoader,
        	 	 	              this.container.getResources());
      
    }

    void stop()  {
        classLoader = null
    }

}