package io.legohunter.imaging.flickr.impl;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.Response;
import com.flickr4java.flickr.Transport;
import io.legohunter.imaging.flickr.config.FlickrProperties;
import io.legohunter.imaging.model.HostedAccountUsage;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * Reads the account's upload count and limit from {@code flickr.people.getUploadStatus}.
 *
 * <p>The Flickr client library parses that response but drops {@code upload_count} and {@code upload_limit}, so
 * the call is made through its transport and those two values are read here. A free account reports a limit of
 * 1000; a Pro account reports -1, which means no limit.
 *
 * <p>Registered as a bean by {@code FlickrConfiguration}, not by component scanning: services import the Flickr
 * classes by name, and a scanned class they do not list would be missing at start-up.
 */
@RequiredArgsConstructor
public class FlickrAccountUsageReader {
    static final String METHOD = "flickr.people.getUploadStatus";

    private final Transport flickrTransport;
    private final FlickrProperties.Secrets flickrSecrets;

    /** Must be called with the Flickr auth set on the request context, like every signed call. */
    public HostedAccountUsage read() throws FlickrException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("method", METHOD);
        Response response = flickrTransport.get(
                flickrTransport.getPath(),
                parameters,
                flickrSecrets.getKey(),
                flickrSecrets.getSecret()
        );
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        return parse(response.getPayload());
    }

    /** {@code user} is the response's {@code <user>} element. */
    static HostedAccountUsage parse(Element user) {
        Integer count = number(user, "upload_count");
        Integer limit = number(user, "upload_limit");
        return new HostedAccountUsage(count, limit == null || limit < 0 ? null : limit);
    }

    /** The value as an attribute of the element or, failing that, as the text of a child element. */
    private static Integer number(Element user, String name) {
        if (user == null) {
            return null;
        }
        String value = user.hasAttribute(name) ? user.getAttribute(name) : null;
        if (value == null) {
            NodeList children = user.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element element && name.equals(element.getTagName())) {
                    value = element.getTextContent();
                    break;
                }
            }
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
