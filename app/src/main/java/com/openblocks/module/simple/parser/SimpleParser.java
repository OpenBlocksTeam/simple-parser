package com.openblocks.module.simple.parser;

import android.content.Context;

import androidx.annotation.NonNull;

import com.openblocks.moduleinterface.OpenBlocksModule;
import com.openblocks.moduleinterface.callbacks.Logger;
import com.openblocks.moduleinterface.exceptions.ParseException;
import com.openblocks.moduleinterface.models.OpenBlocksFile;
import com.openblocks.moduleinterface.models.OpenBlocksProjectMetadata;
import com.openblocks.moduleinterface.models.OpenBlocksRawProject;
import com.openblocks.moduleinterface.models.code.BlockCode;
import com.openblocks.moduleinterface.models.config.OpenBlocksConfig;
import com.openblocks.moduleinterface.models.layout.LayoutViewXMLAttribute;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksCode;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SimpleParser implements OpenBlocksModule.ProjectParser {

    @Override
    public Type getType() {
        return Type.PROJECT_PARSER;
    }

    @Override
    public void initialize(Context context, Logger logger) {

    }

    @Override
    public OpenBlocksConfig setupConfig() {
        return new OpenBlocksConfig();
    }

    @Override
    public void applyConfig(OpenBlocksConfig config) { }

    @Override
    public String generateFreeId(ArrayList<String> existing_ids) {
        return null;
    }

    @NonNull
    @Override
    public OpenBlocksLayout parseLayout(OpenBlocksRawProject project) throws ParseException {
        return null;
    }

    @NonNull
    @Override
    public OpenBlocksCode parseCode(OpenBlocksRawProject project) throws ParseException {
        return null;
    }

    @NonNull
    @Override
    public OpenBlocksProjectMetadata parseMetadata(OpenBlocksRawProject project) throws ParseException {
        return null;
    }

    @NonNull
    @Override
    public OpenBlocksRawProject saveProject(OpenBlocksProjectMetadata metadata, OpenBlocksCode code, OpenBlocksLayout layout) {

        OpenBlocksRawProject rawProject = new OpenBlocksRawProject(
                null, /* The project ID will be set by OpenBlocks itself */
                new ArrayList<>()
        );

        String metadata_serialized = "";

        try {
            metadata_serialized = serializeMetadata(metadata);
        } catch (JSONException ignored) { /* It's pretty impossible for this error to occur */ }

        rawProject.files.add(new OpenBlocksFile(metadata_serialized.getBytes(), "metadata"));



        String code_serialized = "";

        try {
            code_serialized = serializeCode(code);
        } catch (JSONException ignored) { /* It's pretty impossible for this error to occur */ }

        rawProject.files.add(new OpenBlocksFile(code_serialized.getBytes(), "code"));



        String layout_serialized = "";

        try {
            layout_serialized = serializeLayout(layout);
        } catch (JSONException ignored) { /* It's pretty impossible for this error to occur */ }

        rawProject.files.add(new OpenBlocksFile(layout_serialized.getBytes(), "layout"));

        return rawProject;
    }

    private String serializeMetadata(OpenBlocksProjectMetadata metadata) throws JSONException {
        String name = metadata.getName();
        String package_name = metadata.getPackageName();
        String version_name = metadata.getVersionName();
        int version_code = metadata.getVersionCode();

        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("package_name", package_name);
        object.put("version_name", version_name);
        object.put("version_code", version_code);

        return object.toString();
    }

    private String serializeCode(OpenBlocksCode code) throws JSONException {
        JSONObject object = new JSONObject();

        // TODO: 3/24/21 Remove code_templates, because BlockCollection's ParseBlockTask cannot be serialized

        JSONArray array = new JSONArray();

        for (BlockCode block : code.blocks) {

            JSONObject block_json = new JSONObject();

            block_json.put("opcode", block.opcode);
            block_json.put("params", block.parameters);

            array.put(block_json);
        }

        object.put("blocks", array);

        return object.toString();
    }

    private String serializeLayout(OpenBlocksLayout layout) throws JSONException {
        return serializeLayoutChild(layout).toString();
    }

    private JSONObject serializeLayoutChild(OpenBlocksLayout layout) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("view_name", layout.view_name);

        JSONArray parent_attributes = new JSONArray();

        for (LayoutViewXMLAttribute xml_attribute : layout.xml_attributes) {
            JSONObject attributes = new JSONObject();
            attributes.put("prefix", xml_attribute.prefix);
            attributes.put("name", xml_attribute.attribute_name);
            attributes.put("value", xml_attribute.value);

            parent_attributes.put(attributes);
        }

        JSONArray childs = new JSONArray();

        for (OpenBlocksLayout child : layout.childs) {
            childs.put(serializeLayoutChild(child));
        }

        object.put("childs", childs);

        return object;
    }
}
