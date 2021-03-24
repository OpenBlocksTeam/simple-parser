package com.openblocks.module.simple.parser;

import android.content.Context;

import androidx.annotation.NonNull;

import com.openblocks.moduleinterface.OpenBlocksModule;
import com.openblocks.moduleinterface.callbacks.Logger;
import com.openblocks.moduleinterface.exceptions.ParseException;
import com.openblocks.moduleinterface.models.OpenBlocksProjectMetadata;
import com.openblocks.moduleinterface.models.OpenBlocksRawProject;
import com.openblocks.moduleinterface.models.config.OpenBlocksConfig;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksCode;
import com.openblocks.moduleinterface.projectfiles.OpenBlocksLayout;

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
        return null;
    }

    @Override
    public void applyConfig(OpenBlocksConfig config) {

    }

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
        return null;
    }
}
