/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2019 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.internal.models.v1;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.models.EmbedDC;
import com.adobe.cq.wcm.core.components.services.embed.UrlProcessor;
import com.day.cq.wcm.api.designer.Style;
import com.drew.lang.StringUtil;
import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = {EmbedDC.class, ComponentExporter.class},
    resourceType = EmbedDCImpl.RESOURCE_TYPE
)
@Exporter(
    name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
    extensions = ExporterConstants.SLING_MODEL_EXTENSION
)
public class EmbedDCImpl implements EmbedDC {

    protected static final String RESOURCE_TYPE = "core/wcm/components/embed/v_dc/embed";

    @ValueMapValue(name = PN_TYPE, injectionStrategy = InjectionStrategy.OPTIONAL)
    private String type;

    @ValueMapValue(name = PN_URL, injectionStrategy = InjectionStrategy.OPTIONAL)
    private String url;

    @ValueMapValue(name = PN_HTML, injectionStrategy = InjectionStrategy.OPTIONAL)
    private String html;

    @ValueMapValue(name = PN_EMBEDDABLE_RESOURCE_TYPE, injectionStrategy = InjectionStrategy.OPTIONAL)
    private String embeddableResourceType;

    @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
    private Style currentStyle;

    @Inject
    @Optional
    private List<UrlProcessor> urlProcessors;

    @Inject
    private Resource resource;

    private Type embedType;
    private UrlProcessor.Result result;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    private void initModel() {
        embedType = Type.fromString(type);
        if (embedType == null || embedType != Type.URL) {
            url = null;
        }
        if (embedType == null || embedType != Type.HTML) {
            html = null;
        }
        if (embedType == null || embedType != Type.EMBEDDABLE) {
            embeddableResourceType = null;
        }
        if (currentStyle != null) {
            boolean urlDisabled = currentStyle.get(PN_DESIGN_URL_DISABLED, false);
            boolean htmlDisabled = currentStyle.get(PN_DESIGN_HTML_DISABLED, false);
            boolean embeddablesDisabled = currentStyle.get(PN_DESIGN_EMBEDDABLES_DISABLED, false);
            if (urlDisabled) {
                url = null;
            }
            if (htmlDisabled) {
                html = null;
            } else {
                String htmlFileName = html.substring(html.lastIndexOf("/") + 1);
                String jsName = htmlFileName.replace(".html", ".js");
                logger.info("Html Name: {}. JS Name: {}", htmlFileName, jsName);
                String htmlPath = html + "/jcr:content/renditions/original/jcr:content";
                String jsPath = htmlPath.replace(".html", ".js");
                logger.info("Html Path: [{}]. JS Path: [{}]", htmlPath, jsPath);
                ResourceResolver resolver = resource.getResourceResolver();
                String htmlContent = readContentFromNode(htmlPath, resolver);
                String jsContent = readContentFromNode(jsPath, resolver);
                if (StringUtils.isNotEmpty(htmlContent)) {
                    logger.info("Is JS content null? {}", StringUtils.isEmpty(jsContent));
                    if (StringUtils.isNotEmpty(jsContent))
                        htmlContent = htmlContent.replace("<script src=\"" + jsName + "\"></script>",
                            "<script type=\"text/javascript\">" + jsContent + "</script>");
                    html = htmlContent;
                }

            }
            if (embeddablesDisabled) {
                embeddableResourceType = null;
            }
        }
        if (StringUtils.isNotEmpty(url) && urlProcessors != null) {
            for (UrlProcessor urlProcessor : urlProcessors) {
                UrlProcessor.Result result = urlProcessor.process(url);
                if (result != null) {
                    this.result = result;
                    break;
                }
            }
        }
    }

    /**
     * @param path
     * @param resolver
     * @return
     */
    private String readContentFromNode(String path, ResourceResolver resolver) {
        String content = null;
        Resource contentResource = resolver.getResource(path);
        if (null != contentResource) {
            Node node = contentResource.adaptTo(Node.class);
            try {
                InputStream contentStream = null != node ? node.getProperty("jcr:data").getStream() : null;
                InputStreamReader reader = new InputStreamReader(contentStream, "UTF-8");
                content = CharStreams.toString(reader);
            } catch (RepositoryException | IOException e) {
                logger.error("Error occurred in reading node: {}", e.getMessage(), e);
            }
        }
        return content;
    }

    @Nullable
    @Override
    public Type getType() {
        return embedType;
    }

    @Nullable
    @Override
    public String getUrl() {
        return url;
    }

    @Nullable
    @Override
    public UrlProcessor.Result getResult() {
        return result;
    }

    @Nullable
    @Override
    public String getHtml() {
        return html;
    }

    @Nullable
    @Override
    public String getEmbeddableResourceType() {
        return embeddableResourceType;
    }

    @NotNull
    @Override
    public String getExportedType() {
        return resource.getResourceType();
    }
}
