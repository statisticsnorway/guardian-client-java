module no.ssb.guardian.client {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.github.benmanes.caffeine;
    requires info.picocli;
    requires java.net.http;
    requires jjwt.api;
    requires logback.classic;
    requires logback.core;
    requires lombok;
    requires org.slf4j;

    opens no.ssb.guardian.client;

    exports no.ssb.guardian.client;
}
