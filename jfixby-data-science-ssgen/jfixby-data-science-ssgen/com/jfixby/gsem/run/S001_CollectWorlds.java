
package com.jfixby.gsem.run;

import java.io.IOException;

import com.jfixby.scarabei.adopted.gdx.json.GdxJson;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.desktop.ScarabeiDesktop;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.FilesList;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.math.IntegerMath;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpCall;
import com.jfixby.scarabei.api.net.http.HttpCallExecutor;
import com.jfixby.scarabei.api.net.http.HttpCallParams;
import com.jfixby.scarabei.api.net.http.HttpCallProgress;
import com.jfixby.scarabei.api.net.http.HttpURL;
import com.jfixby.scarabei.api.net.http.METHOD;

public class S001_CollectWorlds {

	public static void main (final String[] args) throws IOException {
		ScarabeiDesktop.deploy();
		Json.installComponent(new GdxJson());
		final File chars_file = LocalFileSystem.ApplicationHome().child("exclude-chars.txt");
		final String chars = chars_file.readToString();
		int K = 0;
		final WordsSorter sorter = new WordsSorter();

		final File raw_folder = LocalFileSystem.ApplicationHome().child("raw");
		final FilesList list = raw_folder.listDirectChildren();

		for (int i = 0; i < list.size(); i++) {
			final File file = list.getElementAt(i);
			if (file.getName().startsWith("#")) {
				continue;
			}

			K++;
			String data = null;
			L.d("reading", file);
			data = file.readToString();
			data = data.replaceAll("\\<[^>]*>", "");

			int begin = data.indexOf("Contents");
			begin = (int)IntegerMath.max(begin, 0);
			int end = data.indexOf("References");
			final int max = data.length();
			end = (int)IntegerMath.min(end, max);
			end = data.indexOf("References", end + 1);
			end = (int)IntegerMath.min(end, max);
			if (begin > end) {
				begin = 0;
				end = data.length();
			}
			data = data.substring(begin, end);
			data = data.replaceAll("\n", " ");
			data = data.replaceAll("\r", " ");

			data = exclude_chars(data, chars);
			data = data.replaceAll("  ", " ");

			final List<String> split = Collections.newList(data.split(" "));
			// split.print(url_string);
			// split.print("words++");
			sorter.addAll(split);

		}

		K = (K / 8);
		// L.d("cutting", K)
		// K = 2;
		sorter.filter(K);
		sorter.sort();
		sorter.print();
		sorter.saveTo("words.json");

	}

	private static String readURL (final String url_string) throws IOException {
		final HttpURL url = Http.newURL(url_string);
		L.d("calling", url);

		final HttpCallParams call_scecs = Http.newCallParams();
		call_scecs.setURL(url);
		call_scecs.setMethod(METHOD.GET);
		call_scecs.setUseAgent(true);
		call_scecs.setUseSSL(false);

		final HttpCall call = Http.newCall(call_scecs);

		final HttpCallExecutor exec = Http.newCallExecutor();

		final HttpCallProgress result = exec.execute(call);

		// String data = result.readResultAsString("windows-1251");
		final String data = result.readResultAsString("utf-8");

		return data;
	}

	private static String exclude_chars (String data, final String chars) {

		for (int i = 0; i < chars.length(); i++) {
			final char oldChar = chars.charAt(i);
			data = data.replace(oldChar, ' ');
			// data = data.replaceAll(e, " ");
		}
		return data;
	}
}
