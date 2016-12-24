
package com.jfixby.gsem.run;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

import com.jfixby.scarabei.api.collections.Collection;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.collections.Set;
import com.jfixby.scarabei.api.desktop.DesktopSetup;
import com.jfixby.scarabei.api.file.ChildrenList;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.math.FloatMath;
import com.jfixby.scarabei.api.math.IntegerMath;
import com.jfixby.scarabei.api.net.http.HttpCallExecutor;
import com.jfixby.scarabei.api.sys.Sys;
import com.jfixby.scarabei.api.util.JUtils;

public class S002_GenerateSearchString {
	static String template = "https://www.google.com/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=";

	public static void main (final String[] args) throws IOException, URISyntaxException {
		DesktopSetup.deploy();
		Json.installComponent("com.jfixby.cmns.adopted.gdx.json.RedJson");

		final File words_folder = LocalFileSystem.ApplicationHome().child("words");
		final ChildrenList words_files = words_folder.listDirectChildren();
		final WordsSorter sorter = new WordsSorter(false);
		for (int i = 0; i < words_files.size(); i++) {
			final File file = words_files.getElementAt(i);
			final String file_name = file.getName();
			if (file_name.startsWith("#")) {
				continue;
			}
			L.d("reading", file_name);
			final String data = file.readToString();
			L.d("parsing", file_name);
			final WordCollectorFile content = Json.deserializeFromString(WordCollectorFile.class, data);

			final List<WordCollector> split = Collections.newList(content.values);
			L.d("   adding", split.size());
			sorter.addOthers(split);
		}
		sorter.filter(1);
		sorter.sort();

		// sorter.print();

		final Collection<WordCollector> terms_list = sorter.list();
		terms_list.print("terms_list", 0, 500);
		final Random random = new Random();

		final int EXTRACTIONS = 100;
		final int NUMBER_OF_TERMS = 8;

		for (int i = 0;; i++) {
			final List<String> batch = Collections.newList();
			for (int k = 0; k < EXTRACTIONS; k++) {
				final String request = generateRequest(NUMBER_OF_TERMS, terms_list, random);
				batch.add(request);

			}
			batch.print("batch generated");
			for (int k = 0; k < batch.size(); k++) {
				final String request = batch.getElementAt(k);
				final String request_url = template + request.replaceAll(" ", "+");
				L.d(request, request_url);
				openUrl(request_url);
				Sys.sleep(10 * 1000);
			}
		}

	}

	private static void call (final HttpCallExecutor exec, final String request_url, final String request)
		throws IOException, URISyntaxException {
		// HttpURL url = Http.newURL(request_url);
		// HttpCallSpecs specs = Http.newCallSpecs();
		// specs.setURL(url);
		// HttpCall call = Http.newCall(specs);
		// HttpCallProgress result = exec.execute(call);
		// String data_string = result.readResultAsString("utf-8");
		//
		// data_string = data_string.replaceAll("\\<[^>]*>", "");
		// String file_name = request.replaceAll(" ", "_") + ".html";
		// File result_file = LocalFileSystem.ApplicationHome().child("results")
		// .child(file_name);
		// result_file.writeString(data_string);
		openUrl(request_url);

		// L.d(" ", data_string);
	}

	public static void openUrl (final String url) throws IOException, URISyntaxException {
		if (java.awt.Desktop.isDesktopSupported()) {
			final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

			if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
				final java.net.URI uri = new java.net.URI(url);
				desktop.browse(uri);
			}
		}
	}

	private static String generateRequest (final int n, final Collection<WordCollector> terms_list, final Random random) {
		String result = "";

		final Set<String> stack = Collections.newSet();
		for (; stack.size() < n;) {
			double d1 = random.nextDouble();
			d1 = FloatMath.power(d1, 1.0);
			int index = (int)(terms_list.size() * d1);
			index = (int)IntegerMath.limit(0, index, terms_list.size());
			final String term = terms_list.getElementAt(index).getWord();
			stack.addAll(JUtils.split(term, " "));
		}
		// stack.print("");
		for (int k = 0; k < stack.size(); k++) {
			result = result + " " + stack.getElementAt(k);
		}

		return result;
	}
}
