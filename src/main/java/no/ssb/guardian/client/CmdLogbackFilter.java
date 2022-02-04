package no.ssb.guardian.client;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * CmdLogbackFilter can be used for only logging events that are marked with
 * a special "VERBOSE" log marker and only if the GUARDIAN_CLIENT_VERBOSE system property is set.
 */
public class CmdLogbackFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent logEvent) {
        return logEvent.getMarker() == GuardianClient.VERBOSE &&
                System.getProperty(GuardianClient.GUARDIAN_CLIENT_VERBOSE_PROPERTY) != null
                ? FilterReply.ACCEPT
                : FilterReply.DENY;
    }
}
