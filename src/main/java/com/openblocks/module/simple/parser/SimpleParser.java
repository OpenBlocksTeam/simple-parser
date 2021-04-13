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
import com.openblocks.moduleinterface.models.code.BlockCodeNest;
import com.openblocks.moduleinterface.models.config.OpenBlocksConfig;
import com.openblocks.moduleinterface.models.layout.LayoutViewXMLAttribute;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksCode;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class SimpleParser implements OpenBlocksModule.ProjectParser {

    @Override
    public Type getType() {
        return Type.PROJECT_PARSER;
    }

    @Override
    public void initialize(Context context, Logger logger) {
        logger.trace(this.getClass(), "Initialize!");
    }

    @Override
    public OpenBlocksConfig setupConfig() {
        return new OpenBlocksConfig();
    }

    @Override
    public void applyConfig(OpenBlocksConfig config) { }

    @Override
    public String generateFreeId(ArrayList<String> existing_ids) {

        String id;

        do {
            id = UUID.randomUUID().toString();
        } while (!existing_ids.contains(id));

        return id;
    }

    @Override
    public OpenBlocksLayout parseLayout(OpenBlocksRawProject project) throws ParseException {

        String layout_data = null;

        for (OpenBlocksFile file : project.files) {
            if (file.name.equals("layout")) {
                layout_data = new String(file.data, StandardCharsets.UTF_8);
                break;
            }
        }

        if (layout_data == null) {
            throw new ParseException("layout file doesn't exist");
        }

        OpenBlocksLayout parsed_layout;

        try {
            parsed_layout = parseLayout(new JSONObject(layout_data));
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ParseException("JSONObject failed to parse layout data: " + e.getMessage());
        }

        return parsed_layout;
    }

    private OpenBlocksLayout parseLayout(JSONObject layout) throws JSONException {
        ArrayList<LayoutViewXMLAttribute> attributes = new ArrayList<>();
        ArrayList<OpenBlocksLayout> childs = new ArrayList<>();
        String view_name;

        view_name = layout.getString("name");

        JSONArray attributes_json = layout.getJSONArray("attributes");
        for (int i = 0; i < attributes_json.length(); i++) {
            JSONObject attribute = attributes_json.getJSONObject(i);
            attributes.add(
                    new LayoutViewXMLAttribute(
                            attribute.getString("prefix"),
                            attribute.getString("name"),
                            attribute.get("value")
                    )
            );
        }

        JSONArray childs_array = layout.getJSONArray("childs");
        for (int i = 0; i < childs_array.length(); i++) {
            JSONObject child = childs_array.getJSONObject(i);

            childs.add(parseLayout(child));
        }

        return new OpenBlocksLayout(childs, view_name, attributes);
    }

    @Override
    public OpenBlocksCode parseCode(OpenBlocksRawProject project) throws ParseException {
        String code_data = null;

        for (OpenBlocksFile file : project.files) {
            if (file.name.equals("code")) {
                code_data = new String(file.data, StandardCharsets.UTF_8);
                break;
            }
        }

        if (code_data == null) {
            throw new ParseException("code file doesn't exist");
        }

        OpenBlocksCode parsed_code;

        try {
            ArrayList<BlockCode> block_codes = new ArrayList<>();

            JSONObject code = new JSONObject(code_data);
            String block_collection_name = code.getString("block_collection");
            JSONArray blocks = code.getJSONArray("blocks");

            for (int i = 0; i < blocks.length(); i++) {
                block_codes.add(parseBlockCode(blocks.getJSONObject(i)));
            }

            parsed_code = new OpenBlocksCode(block_collection_name, block_codes);

        } catch (JSONException e) {
            e.printStackTrace();

            throw new ParseException("JSONObject failed to parse layout data: " + e.getMessage());
        }

        return parsed_code;
    }

    private BlockCode parseBlockCode(JSONObject object) throws JSONException {
        BlockCode blockCode = null;
        String opcode;
        ArrayList<String> params = new ArrayList<>();

        opcode = object.getString("opcode");

        JSONArray params_array = object.getJSONArray("params");

        for (int i = 0; i < params_array.length(); i++) {
            params.add((String) params_array.get(i));
        }
        
        if (object.has("childs")) {
            ArrayList<BlockCode> childs = new ArrayList<>();
            
            JSONArray childs_array = object.getJSONArray("childs");

            for (int i = 0; i < childs_array.length(); i++) {
                childs.add(parseBlockCode(childs_array.getJSONObject(i)));
            }

            blockCode = new BlockCodeNest(opcode, params, childs);
            
        } else {
            blockCode = new BlockCode(opcode, params);
        }

        return blockCode;
    }

    @Override
    public OpenBlocksProjectMetadata parseMetadata(OpenBlocksRawProject project) throws ParseException {
        String metadata_data = null;

        for (OpenBlocksFile file : project.files) {
            if (file.name.equals("metadata")) {
                metadata_data = new String(file.data, StandardCharsets.UTF_8);
                break;
            }
        }

        if (metadata_data == null) {
            throw new ParseException("metadata file doesn't exist");
        }

        try {
            JSONObject metadata = new JSONObject(metadata_data);
            String name = metadata.getString("name");
            String package_name = metadata.getString("package_name");
            String version_name = metadata.getString("version_name");
            int version_code = metadata.getInt("version_code");

            return new OpenBlocksProjectMetadata(name, package_name, version_name, version_code);

        } catch (JSONException ignored) {}

        // ok return nulls instead
        return new OpenBlocksProjectMetadata(null, null, null, 0);
    }

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

        object.put("block_collection", code.block_collection_name);

        JSONArray array = new JSONArray();

        for (BlockCode block : code.blocks) {
            array.put(serializeBlockCode(block));
        }

        object.put("blocks", array);

        return object.toString();
    }

    private JSONObject serializeBlockCode(BlockCode block) throws JSONException {

        JSONObject block_json = new JSONObject();

        block_json.put("opcode", block.opcode);
        block_json.put("params", new JSONArray(block.parameters));

        if (block instanceof BlockCodeNest) {
            JSONArray childs = new JSONArray();

            for (BlockCode blockCode : ((BlockCodeNest) block).blocks) {
                childs.put(serializeBlockCode(blockCode));
            }

            block_json.put("childs", childs);
        }

        return block_json;
    }

    private String serializeLayout(OpenBlocksLayout layout) throws JSONException {
        return serializeLayoutChild(layout).toString();
    }

    private JSONObject serializeLayoutChild(OpenBlocksLayout layout) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", layout.view_name);

        JSONArray parent_attributes = new JSONArray();

        // Yes this is very unoptimized, this module is just for testing
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
