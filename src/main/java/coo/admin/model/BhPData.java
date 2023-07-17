package coo.admin.model;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Alias("bhPD")
@Data
public class BhPData {

	Integer limit = 15, pageLimit=5, start, nowPage;
	Integer startPage, endPage, totalPage;
	String sch, startD, endD, cate, kind;
	

	public void setTotal(int total) {
		totalPage = total / limit;
		if(total % limit != 0) {
			totalPage++;
		}
		start = (nowPage-1) * limit;
		startPage = (nowPage-1)/ pageLimit * pageLimit +1;
		endPage = startPage + pageLimit - 1;
		
		if(endPage>totalPage) {
			endPage = totalPage;
		}
	}
	
	
	
	
	
}