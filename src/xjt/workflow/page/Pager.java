package xjt.workflow.page;

import java.util.List;

public class Pager {
	public final static int PAGESIZE = 30;
	// 每页数
	private int pageSize = PAGESIZE;
	// 分页项
	private List items;
	// 所有记录数
	private int totalCount;
	// 索引数组
	private int[] indexes = new int[0];
	// 开始索引值
	private int startIndex = 0;
	// 所有分页 数
	private int totalPage = 0;
	// 当前页数
	private int currentPage = 0;

	public Pager(List items, int totalCount) {
		setPageSize(PAGESIZE);
		setTotalCount(totalCount);
		setItems(items);
		setStartIndex(0);
		calcPage();
	}

	public Pager(List items, int totalCount, int startIndex) {
		setPageSize(PAGESIZE);
		setTotalCount(totalCount);
		setItems(items);
		setStartIndex(startIndex);
		calcPage();
	}

	public Pager(List items, int totalCount, int pageSize, int startIndex) {
		setPageSize(pageSize);
		setTotalCount(totalCount);
		setItems(items);
		System.out.println(items.size());
		setStartIndex(startIndex);
		calcPage();
	}

	// 计算
	private void calcPage() {
		// System.out.println(totalCount+" "+pageSize+" "+totalPage+"
		// "+currentPage);
		currentPage = startIndex / pageSize + 1;
		if (totalCount != 0) {
			if ((totalCount % pageSize) == 0) {
				totalPage = totalCount / pageSize;
			} else {
				totalPage = (totalCount / pageSize) + 1;
			}

			if (currentPage > totalPage) {
				currentPage = totalPage;
			}

			if (currentPage < 1) {
				currentPage = 1;
			}
		} else {
			currentPage = 0;
		}
		// System.out.println(totalCount+" "+pageSize+" "+totalPage+"
		// "+currentPage);
	}

	public List getItems() {
		return items;
	}

	public void setItems(List items) {
		this.items = items;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		if (totalCount > 0) {
			this.totalCount = totalCount;
			int count = totalCount / pageSize;
			if (totalCount % pageSize > 0)
				count++;
			indexes = new int[count];
			for (int i = 0; i < count; i++) {
				indexes[i] = pageSize * i;
			}
		} else {
			this.totalCount = 0;
		}
	}

	public int[] getIndexes() {
		return indexes;
	}

	public void setIndexes(int[] indexes) {
		this.indexes = indexes;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		if (totalCount <= 0)
			this.startIndex = 0;
		else if (startIndex >= totalCount)
			this.startIndex = indexes[indexes.length - 1];
		else if (startIndex < 0)
			this.startIndex = 0;
		else {
			this.startIndex = indexes[startIndex / pageSize];
		}
	}

	public int getNextIndex() {
		int nextIndex = getStartIndex() + pageSize;
		if (nextIndex >= totalCount)
			return getStartIndex();
		else
			return nextIndex;
	}

	public int getPreviousIndex() {
		int previousIndex = getStartIndex() - pageSize;
		if (previousIndex < 0)
			return 0;
		else
			return previousIndex;
	}
}
