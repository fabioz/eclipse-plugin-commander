package dakara.eclipse.plugin.stringscore;

import java.util.HashMap;
import java.util.Map;

import dakara.eclipse.plugin.stringscore.StringScore.Score;

public final class RankedItem<T> {
	public final T dataItem;
	private Map<String, Score> scores = new HashMap<>();
	private boolean scorePerColumn = false;
	public int order = 0;
	public RankedItem(T dataItem) {
		this.dataItem = dataItem;
	}
	public RankedItem(T dataItem, int order) {
		this.dataItem = dataItem;
		this.order = order;
	}
	public T getDataItem() {
		return dataItem;
	}
	public void addScore(Score score, String fieldId) {
		scores.put(fieldId, score);
	}
	public Score getColumnScore(String fieldId) {
		return scores.get(fieldId);
	}
	
	public void setScoreModeByColumn(boolean scorePerColumn) {
		this.scorePerColumn = scorePerColumn;
	}
	
	public int totalScore() {
		int sum = 0;
		if (scorePerColumn) {
			for (Score score : scores.values()) {
				if (score.rank == 0) return 0;
				if (score.rank < 0) continue;
				sum += score.rank;
			}

			return sum;
		}

		// Each score has the same rank when not scoring per column.  It the the score of the entire row.
		for (Score score : scores.values()) {
			return score.rank;
		}
		return sum;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (!(obj instanceof RankedItem)) return false;
		
		return dataItem.equals(((RankedItem)obj).dataItem);
	};
	
	@Override
	public int hashCode() {
		return dataItem.hashCode();
	}
	
	public static final class RankedItemFactory<T> {
		private static int order = 0;
		public RankedItem<T> make(T dataItem) {
			return new RankedItem<T>(dataItem, order++);
		}
	}
}