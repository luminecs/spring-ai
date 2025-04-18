package org.springframework.ai.reader.pdf.layout;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;

public class PDFLayoutTextStripperByArea extends ForkPDFLayoutTextStripper {

	private final List<String> regions = new ArrayList<String>();

	private final Map<String, Rectangle2D> regionArea = new HashMap<String, Rectangle2D>();

	private final Map<String, ArrayList<List<TextPosition>>> regionCharacterList = new HashMap<String, ArrayList<List<TextPosition>>>();

	private final Map<String, StringWriter> regionText = new HashMap<String, StringWriter>();

	public PDFLayoutTextStripperByArea() throws IOException {
		super.setShouldSeparateByBeads(false);
	}

	@Override
	public final void setShouldSeparateByBeads(boolean aShouldSeparateByBeads) {
	}

	public void addRegion(String regionName, Rectangle2D rect) {
		this.regions.add(regionName);
		this.regionArea.put(regionName, rect);
	}

	public void removeRegion(String regionName) {
		this.regions.remove(regionName);
		this.regionArea.remove(regionName);
	}

	public List<String> getRegions() {
		return this.regions;
	}

	public String getTextForRegion(String regionName) {
		StringWriter text = this.regionText.get(regionName);
		return text.toString();
	}

	public void extractRegions(PDPage page) throws IOException {
		for (String regionName : this.regions) {
			setStartPage(getCurrentPageNo());
			setEndPage(getCurrentPageNo());

			ArrayList<List<TextPosition>> regionCharactersByArticle = new ArrayList<List<TextPosition>>();
			regionCharactersByArticle.add(new ArrayList<TextPosition>());
			this.regionCharacterList.put(regionName, regionCharactersByArticle);
			this.regionText.put(regionName, new StringWriter());
		}

		if (page.hasContents()) {
			processPage(page);
		}
	}

	@Override
	protected void processTextPosition(TextPosition text) {
		for (Map.Entry<String, Rectangle2D> regionAreaEntry : this.regionArea.entrySet()) {
			Rectangle2D rect = regionAreaEntry.getValue();
			if (rect.contains(text.getX(), text.getY())) {
				this.charactersByArticle = this.regionCharacterList.get(regionAreaEntry.getKey());
				super.processTextPosition(text);
			}
		}
	}

	@Override
	protected void writePage() throws IOException {
		for (String region : this.regionArea.keySet()) {
			this.charactersByArticle = this.regionCharacterList.get(region);
			this.output = this.regionText.get(region);
			super.writePage();
		}
	}

}
