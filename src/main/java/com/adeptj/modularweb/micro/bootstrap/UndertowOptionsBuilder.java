package com.adeptj.modularweb.micro.bootstrap;

import java.util.HashSet;
import java.util.Set;

import org.xnio.Option;

import com.typesafe.config.Config;

import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;

/**
 * Undertow Server Options.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class UndertowOptionsBuilder {

	private static final Set<Option<Long>> OPTIONS_LONG;

	private static final Set<Option<Integer>> OPTIONS_INT;

	private static final Set<Option<Boolean>> OPTIONS_BOOL;

	static {
		OPTIONS_LONG = new HashSet<>();
		OPTIONS_LONG.add(UndertowOptions.MAX_ENTITY_SIZE);
		OPTIONS_LONG.add(UndertowOptions.MULTIPART_MAX_ENTITY_SIZE);

		OPTIONS_INT = new HashSet<>();
		OPTIONS_INT.add(UndertowOptions.MAX_HEADER_SIZE);

		OPTIONS_BOOL = new HashSet<>();
		OPTIONS_BOOL.add(UndertowOptions.ALWAYS_SET_KEEP_ALIVE);
		OPTIONS_BOOL.add(UndertowOptions.ALWAYS_SET_DATE);
	}

	public static void build(Builder builder, Config undertowConf) {
		Config connConfig = undertowConf.getConfig("connection-options");
		OPTIONS_LONG.forEach(option -> builder.setServerOption(option, connConfig.getLong(option.getName())));
		OPTIONS_INT.forEach(option -> builder.setServerOption(option, connConfig.getInt(option.getName())));
		OPTIONS_BOOL.forEach(option -> builder.setServerOption(option, connConfig.getBoolean(option.getName())));
	}
}
