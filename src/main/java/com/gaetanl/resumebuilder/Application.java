package com.gaetanl.resumebuilder;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Entry point in case the embedded Tomcat is needed, if not, this class can be
 * deleted entirely as no entry point is needed.
 * @see <a href="https://stackoverflow.com/questions/57652798/is-it-necessary-to-use-main-method-when-we-develop-web-application-using-spring">...</a>
 */
@ComponentScan
@EnableJpaRepositories
public class Application {
    public static final Integer port = Integer.valueOf(Optional.ofNullable(System.getenv("PORT")).orElse("8080"));

    public static void main(String[] args) throws IOException, LifecycleException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
        Application app = ctx.getBean(Application.class);
        app.run(args);
    }

    public void run(String[] args) throws IOException, LifecycleException {

        // Location of the .war, relative to the project root
        // i.e. where the README.md is located
        String gradleWarBuildDirectory = "/build/libs";

        // Name of the .war, extension excluded, normally
        // project-name + "-1.0-SNAPSHOT" by default
        String warName = "resume-builder-1.0-SNAPSHOT";

        // The base uri at which the app will be exposed by the server
        // i.e. http://localhost:8080/rootUriExposed/api/user
        String rootUriExposed = warName;

        // Base folder created by the embedded Tomcat,
        // relative to the project root
        String tomcatBaseDir = "/tomcat.8080";

        // Tomcat webapps folder, relative to tomcatBaseDir
        String tomcatWebappsDir = "/work/Tomcat/localhost";

        File war = new File("." + gradleWarBuildDirectory + "/" + warName + ".war");
        if (!war.exists()) {
            System.err.println(warName + ".war has not been created yet, or is not located in ./build/libs");
            System.err.println("Verify that the \"war\" plugin is added in build.gradle, then try running the gradle task \"assemble\"");
            return;
        }

        /*
         * Recreating directories if they don't exist to avoid IOException.
         * Be careful NOT to create the proper webapp folder here or
         * tomcat.addWebapp() won't unpack the .war inside. Stop at the webapp
         * folder's parent.
         * e.g.: here the webapp will be in /tomcat.8080/work/Tomcat/localhost/demo-spring-api-1.0-SNAPSHOT,
         * so I create up to /tomcat.8080/work/Tomcat/localhost
         */
        File thisWebappDir = new File("." + tomcatBaseDir + tomcatWebappsDir + "/" + warName);
        Path thisWebappDirPath = Path.of(thisWebappDir.getCanonicalPath());
        if (Files.exists(thisWebappDirPath, LinkOption.NOFOLLOW_LINKS)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
            String prefix = dtf.format(LocalDateTime.now());
            File renamed = new File("." + tomcatBaseDir + tomcatWebappsDir + "/" + prefix + warName);
            thisWebappDir.renameTo(renamed);
        }
        File tomcatWebappsFromProjectRoot = new File("." + tomcatBaseDir + tomcatWebappsDir);
        Files.createDirectories(Path.of(tomcatWebappsFromProjectRoot.getCanonicalPath()));

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        tomcat.setBaseDir("." + tomcatBaseDir);
        tomcat.getHost().setAppBase("." + tomcatWebappsDir);
        tomcat.addWebapp("/" + rootUriExposed, war.getCanonicalPath());

        System.out.println("Starting server...");
        tomcat.start();

        System.out.println("Server started");
        System.out.println();
        System.out.println("curl refresher:");
        System.out.println("- Use \"curl.exe\" instead of \"curl\" while in PowerShell Desktop");
        System.out.println("- Option -v to get verbose response");
        System.out.println("- Option -X to specify request method (GET, POST, etc.)");
        System.out.println();
        System.out.println("Try the following requests:");
        System.out.println("  curl -v http://localhost:" + port + "/" + rootUriExposed + "/api/user");
        System.out.println("    ↳ Implicit GET request without parameters, should return 200 OK + content \"ApiController.getUsers() response\"");
        tomcat.getServer().await();
    }
}
