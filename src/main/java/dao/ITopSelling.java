package dao;

import java.util.List;

import model.TopSellingProduct;

public interface ITopSelling {
	List<TopSellingProduct> getTopSellingProducts(int limit, int offset);
	int getTotalProductCount();
	
    List<TopSellingProduct> searchTopSellingProducts(String keyword, int limit, int offset);
    int getTotalProductCountBySearch(String keyword);
}
