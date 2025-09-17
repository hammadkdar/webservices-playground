package ae.etisalat.cisapp.config;

import ae.etisalat.cisapp.interceptor.LoggingWebServiceInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import java.time.Duration;

@Configuration
public class WebServiceConfig {

    @Value("${webservice.cis.url:http://localhost:8080/soap/cis}")
    private String defaultUri;

    @Value("${webservice.cis.context-path:sem.cis.pull.cbcm}")
    private String contextPath;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        // Use explicit class binding instead of context path since the JAR structure
        // might not have the required JAXB metadata files (ObjectFactory or jaxb.index)
        marshaller.setClassesToBeBound(
                sem.cis.pull.cbcm.GetSubscriptionDtls.class,
                sem.cis.pull.cbcm.GetSubscriptionDtlsResponse.class
        );

        // Enable support for classes without @XmlRootElement annotation
        marshaller.setSupportJaxbElementClass(true);

        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller, LoggingWebServiceInterceptor loggingInterceptor) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        webServiceTemplate.setDefaultUri(defaultUri);

        // Use custom message sender with enhanced logging capabilities
        LoggingHttpUrlConnectionMessageSender messageSender = new LoggingHttpUrlConnectionMessageSender();
        messageSender.setConnectionTimeout(Duration.ofSeconds(30)); // 30 seconds
        messageSender.setReadTimeout(Duration.ofSeconds(60)); // 60 seconds
        webServiceTemplate.setMessageSender(messageSender);

        // Add logging interceptor
        webServiceTemplate.setInterceptors(new ClientInterceptor[]{loggingInterceptor});

        return webServiceTemplate;
    }
}
