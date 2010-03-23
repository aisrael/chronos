grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {        
        grailsPlugins()
        grailsHome()
        
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        
        // runtime 'mysql:mysql-connector-java:5.1.5'
        compile 'opensymphony:quartz:1.7.3'
    }
    
}

grails.war.resources = { stagingDir ->
    // remove Quartz JAR (and use provided Quartz JAR)
    delete(file:"${stagingDir}/WEB-INF/lib/quartz-1.7.3.jar")
    // remove SLF4J to Log4J binding for JBoss (uses provided SLF4J to JBoss binding) 
    delete(file:"${stagingDir}/WEB-INF/lib/slf4j-log4j12-1.5.8.jar")
}
