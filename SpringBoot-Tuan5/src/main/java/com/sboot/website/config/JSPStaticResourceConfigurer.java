package com.sboot.website.config;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.WebResourceRoot;
import org.springframework.util.ResourceUtils;

import java.net.URI;
import java.net.URL;

public class JSPStaticResourceConfigurer implements LifecycleListener {

    private final Context context;

    public JSPStaticResourceConfigurer(Context context) {
        this.context = context;
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if (!event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
            return;
        }

        final URL base = getUrl();
        final String subPath = ResourceUtils.isFileURL(base) ? "/" : "/META-INF";

        try {
            this.context.getResources().createWebResourceSet(
                    WebResourceRoot.ResourceSetType.RESOURCE_JAR,
                    "/",
                    base,
                    subPath
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // Không chặn khởi động Tomcat nếu resource set không hợp lệ ở môi trường hiện tại
            System.out.println("Skip adding WebResourceSet base=" + base + " subPath=" + subPath + " because: " + ex.getMessage());
        }
    }

    private URL getUrl() {
        final URL location = this.getClass().getProtectionDomain().getCodeSource().getLocation();

        if (ResourceUtils.isFileURL(location)) {
            // Chạy từ target/classes
            return location;
        } else if (ResourceUtils.isJarURL(location)) {
            try {
                String locationStr = location.getPath()
                        .replaceFirst("^nested:", "")
                        .replaceFirst("/!BOOT-INF/classes/!/$", "!/");

                return new URI("jar:file", locationStr, null).toURL();

            } catch (Exception e) {
                throw new IllegalStateException("Unable to add new JSP source URI to tomcat resources", e);
            }
        } else {
            throw new IllegalStateException("Can not add tomcat resources, unhandleable url: " + location);
        }
    }
}