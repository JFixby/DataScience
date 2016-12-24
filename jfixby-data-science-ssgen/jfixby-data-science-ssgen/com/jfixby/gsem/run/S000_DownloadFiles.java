package com.jfixby.gsem.run;

import java.io.IOException;

import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.desktop.DesktopSetup;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpCall;
import com.jfixby.scarabei.api.net.http.HttpCallExecutor;
import com.jfixby.scarabei.api.net.http.HttpCallParams;
import com.jfixby.scarabei.api.net.http.HttpCallProgress;
import com.jfixby.scarabei.api.net.http.HttpURL;
import com.jfixby.scarabei.api.net.http.METHOD;
import com.jfixby.scarabei.api.util.JUtils;
import com.jfixby.scarabei.red.net.http.RedHttp;

public class S000_DownloadFiles {

	public static void main(String[] args) throws IOException {
		DesktopSetup.deploy();
		

		File chars_file = LocalFileSystem.ApplicationHome().child("exclude-chars.txt");

		File sources = LocalFileSystem.ApplicationHome().child("word-sources.txt");
		File raw_folder = LocalFileSystem.ApplicationHome().child("raw");
		raw_folder.makeFolder();
		String list = sources.readToString();
		List<String> list_of_sources = JUtils.split(list, "\n");

		for (int i = 0; i < list_of_sources.size(); i++) {
			String url = list_of_sources.getElementAt(i).replaceAll("\r", "").replaceAll("\n", "");

			String data = null;
			L.d("downloading", url);
			data = readURL(url);
			String child_name = url.replaceAll("https", "").replaceAll("http", "").replaceAll("://", "").replaceAll("/", "_") + ".html";
			L.d("          ", child_name);
			File file = raw_folder.child(child_name);
			file.writeString(data);

		}

	}

	private static String readURL(String url_string) throws IOException {
		HttpURL url = Http.newURL(url_string);

		HttpCallParams call_scecs = Http.newCallParams();
		call_scecs.setURL(url);
		call_scecs.setMethod(METHOD.GET);
		call_scecs.setUseAgent(true);
		call_scecs.setUseSSL(false);

		HttpCall call = Http.newCall(call_scecs);

		HttpCallExecutor exec = Http.newCallExecutor();

		HttpCallProgress result = exec.execute(call);

		// String data = result.readResultAsString("windows-1251");
		String data = result.readResultAsString("utf-8");

		return data;
	}

	private static String exclude_chars(String data, String chars) {

		for (int i = 0; i < chars.length(); i++) {
			char oldChar = chars.charAt(i);
			data = data.replace(oldChar, ' ');
			// data = data.replaceAll(e, " ");
		}
		return data;
	}
}
