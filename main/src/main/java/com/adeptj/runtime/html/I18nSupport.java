package com.adeptj.runtime.html;

import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.extension.i18n.UTF8Control;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.adeptj.runtime.common.Constants.VAR_ERROR_CODE;

/**
 * This class replaces the default i18n extension provided by Pebble, this is done to cater AdeptJ Runtime requirements.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class I18nSupport extends AbstractExtension implements Function {

    private final String rbDir;

    private final List<String> argumentNames;

    public I18nSupport(String rbDir) {
        this.rbDir = rbDir;
        this.argumentNames = new ArrayList<>();
        this.argumentNames.add("bundle");
        this.argumentNames.add("key");
        this.argumentNames.add("params");
    }

    // ----------------------------------------- Extension Methods -----------------------------------------

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put("i18n", this);
        return functions;
    }

    // ------------------------------------------ Function Methods ------------------------------------------

    @Override
    public List<String> getArgumentNames() {
        return this.argumentNames;
    }

    @Override
    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
        String basename = (String) args.get("bundle");
        String key = (String) args.get("key");
        Object params = args.get("params");
        ResourceBundle bundle = ResourceBundle.getBundle(this.rbDir + basename, context.getLocale(),
                new UTF8Control());
        Object errorCode = context.getVariable(VAR_ERROR_CODE);
        Object phraseObject;
        if (errorCode == null || StringUtils.equals(key, "go.home.msg")) {
            phraseObject = bundle.getObject(key);
        } else {
            phraseObject = bundle.getObject(errorCode + "." + key);
        }
        if (params != null) {
            if (params instanceof List<?> list) {
                phraseObject = MessageFormat.format(phraseObject.toString(), list.toArray());
            } else {
                phraseObject = MessageFormat.format(phraseObject.toString(), params);
            }
        }
        return phraseObject;
    }
}
