
package com.jfixby.gsem.run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import com.jfixby.scarabei.api.collections.Collection;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.collections.Set;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;

public class WordsSorter {

	private static final boolean COLLECT_SINGLES = false;
	private static final boolean COLLECT_PAIRS = false;
	private static final boolean COLLECT_TRIPLETS = false;
	private static final boolean COLLECT_4 = !false;
	private final Set<String> exclude;
	List<String> history = Collections.newList();
	final Map<String, WordCollector> collectors = Collections.newMap();

	public WordsSorter () throws IOException {
		super();

		final File word_sources = LocalFileSystem.ApplicationHome().child("exclude-words.txt");
		this.exclude = Collections.newSet(word_sources.readToString().split("\r\n"));
		this.exclude.print("exclude");
	}

	public WordsSorter (final boolean b) throws IOException {
		final File word_sources = LocalFileSystem.ApplicationHome().child("exclude-phrases.txt");
		this.exclude = Collections.newSet(word_sources.readToString().split("\r\n"));
		this.exclude.print("exclude");
	}

	private final Comparator<WordCollector> comparator = new Comparator<WordCollector>() {

		@Override
		public int compare (final WordCollector o1, final WordCollector o2) {
			return Integer.compare(o1.n, o2.n);
		}
	};
	private long total;

	public void addAll (final Collection<String> split) {
		for (int i = 0; i < split.size(); i++) {
			this.add(split.getElementAt(i));
		}
	}

	public void addOthers (final Collection<WordCollector> split) {
		for (final WordCollector c : split) {
			this.addOther(c);
		}
	}

	private void addOther (final WordCollector elementAt) {
		final String word = elementAt.getWord();
		if (this.exclude.contains(word)) {
			L.d("skip", word);
			return;
		}

		final WordCollector collector = this.getCollector(word);
		collector.add(elementAt.n);
		this.total = this.total + elementAt.n;
	}

	public void add (String word) {
		if (word.length() == 0) {
			return;
		}
		word = word.toLowerCase();
		if (this.exclude.contains(word)) {
			return;
		}

		this.history.insertElementAt(word, 0);
		if (this.history.size() > 10) {
			this.history.removeLast();
		}
		if (this.history.size() > 1 && COLLECT_PAIRS) {
			final String previous_word = this.history.getElementAt(1);
			final WordCollector collector = this.getCollector(previous_word + " " + word);
			collector.add();
			this.total++;
		}
		if (this.history.size() > 2 && COLLECT_TRIPLETS) {
			final String previous_word = this.history.getElementAt(1);
			final String previous_previous_word = this.history.getElementAt(2);
			final WordCollector collector = this.getCollector(previous_previous_word + " " + previous_word + " " + word);
			collector.add();
			this.total++;
		}
		if (this.history.size() > 3 && COLLECT_4) {
			final String previous_word = this.history.getElementAt(1);
			final String previous_previous_word = this.history.getElementAt(2);
			final String previous_previous_previous_word = this.history.getElementAt(3);
			final WordCollector collector = this
				.getCollector(previous_previous_previous_word + " " + previous_previous_word + " " + previous_word + " " + word);
			collector.add();
			this.total++;
		}

		if (COLLECT_SINGLES) {
			final WordCollector collector = this.getCollector(word);
			collector.add();
			this.total++;
		}

	}

	private WordCollector getCollector (final String word) {
		WordCollector c = this.collectors.get(word);
		if (c == null) {
			// L.d("new collector", word);
			c = new WordCollector(word, this);
			this.collectors.put(word, c);
		} else {
			// L.d(" found", word);
		}

		return c;
	}

	public void print () {
		// List<WordCollector> vals = JUtils.newList(collectors.values());
		// vals.sort(comparator);
		this.collectors.print("");
	}

	public String quantileOf (final int n) {
		return ((int)(n * 100d / this.total)) + "%";
	}

	public void saveTo (final String file_name) throws IOException {

		File word_sources = LocalFileSystem.ApplicationHome().child("words");
		word_sources.makeFolder();
		word_sources = word_sources.child(file_name);
		final WordCollectorFile file = new WordCollectorFile();
		final ArrayList<WordCollector> vals = new ArrayList<WordCollector>();
		vals.addAll(this.collectors.values().toJavaList());
		file.values = vals;
		final String data = Json.serializeToString(file).toString();
		word_sources.writeString(data);

	}

	public void filter (final int S) {
		final Collection<WordCollector> vals = this.collectors.values();
		final List<String> bad = Collections.newList();
		for (long i = 0; i < vals.size(); i++) {
			final WordCollector element = vals.getElementAt(i);
			if (element.n <= S) {
				bad.add(element.getWord());
				L.d("removing", element);
			}
		}
		this.collectors.removeAll(bad);
	}

	public Collection<WordCollector> list () {
		final List<WordCollector> results = Collections.newList();
		for (int i = 0; i < this.collectors.size(); i++) {
			final WordCollector val = this.collectors.getValueAt(i);
			results.add(val);
		}
		return results;

	}

	public void sort () {
		final Collection<WordCollector> list = this.list();
		final List<WordCollector> vals = Collections.newList(list);
		vals.sort(this.comparator);
		vals.reverse();
		this.collectors.clear();
		for (final WordCollector c : vals) {
			this.collectors.put(c.getWord(), c);
		}
	}

	public void clear () {
		this.collectors.clear();
	}
}
