package com.spring.dalhada;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import service.BucketService;
import service.NaverBlogService;
import service.SearchBucketService;
import vo.BucketDetailVO;
import vo.BucketVO;
import vo.EditBucketInfoVO;
import vo.LikeInfoVO;
import vo.NaverBlogVO;
import vo.PagingVO;
import vo.SearchBucketVO;
import vo.StringIntVO;
import vo.TagInfoVO;

import vo.UpdatedBucketVO;
import vo.InsertedBucketVO;

@Controller
@SessionAttributes(value = "searchBucketVO")
public class BucketController {
	@Autowired
	private SearchBucketService searchBucketService;
	@Autowired
	private NaverBlogService naverBlogService;
	@Resource(name="bucketService")
	private BucketService bucketservice;

	@RequestMapping(value="/main")
	public ModelAndView main(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		String member_id = (String) session.getAttribute("id");
	

		List<BucketVO> TOPlist = bucketservice.selectTOPBucket(member_id);
		
		mav.addObject("TOPlist", TOPlist);
		mav.setViewName("main");
		return mav;
	}
	
	@RequestMapping(value="/main/like")
	@ResponseBody
	public String clickheart(HttpSession session, LikeInfoVO vo) {
		int result = 0;
		String id = (String)session.getAttribute("id");
		
		if(id != null) {
			vo.setMember_id(id);
			String action = vo.getAction();
			if(action.equals("insert")) {
				result = bucketservice.insertLikeInfo(vo);
			}else if(action.equals("delete")) {
				result = bucketservice.deleteLikeInfo(vo);
			}
		}else {
			result = -1;
		}
		return result+"";
	}
	
	@RequestMapping(value="/main/getgrouptag")
	@ResponseBody
	public List<List<StringIntVO>> getgrouptag(HttpSession session) {
		String member_id = (String)session.getAttribute("id");
		List<List<StringIntVO>> list = new ArrayList<List<StringIntVO>>();
		
		if(member_id!=null) {
			List<StringIntVO> groups = bucketservice.selectGroups(member_id);
			List<StringIntVO> tags = bucketservice.selectTags();
			list.add(groups);
			list.add(tags);
		}
		return list;
	}
  
	@RequestMapping(value="/main/geteditinfo")
	@ResponseBody
	public EditBucketInfoVO modaledit(HttpSession session, String selectedbucket_id) {
		String member_id = (String) session.getAttribute("id");
		StringIntVO map = new StringIntVO();
		map.setId(Integer.parseInt(selectedbucket_id));
		map.setName(member_id);
		EditBucketInfoVO vo =  bucketservice.getEditInfo(map);
		return vo;
	}   
	
	//좋아요  많은 거 / 추천 버킷
	@RequestMapping(value="/main/modaldetail")
	@ResponseBody
	public BucketDetailVO modaldetail(HttpSession session, String selectedbucket_id) {
		String member_id = (String) session.getAttribute("id");
		StringIntVO map = new StringIntVO();
		map.setId(Integer.parseInt(selectedbucket_id));
		map.setName(member_id);
		BucketDetailVO vo =  bucketservice.selectDetail(map);
		return vo;
	}

	@RequestMapping(value="/createbucket")
	@ResponseBody
	public String createbucket(HttpSession session, InsertedBucketVO vo, @RequestParam(value="g_id")String g_id,
			@RequestParam(value="taglist")List<String> taglist, @RequestParam("file") MultipartFile file) {
		String fileName = file.getOriginalFilename(), filePath, result = "success";
		String member_id = (String) session.getAttribute("id");
		vo.setMember_id(member_id);
		vo.setTag_id(taglist);
		vo.setGroup_id(Integer.parseInt(g_id));
		vo.setImage_path("_"+fileName);
		filePath = bucketservice.insertBucket(vo);
		byte[] image = null;
		try {
			image = file.getBytes();
			File f = new File("C:/unico/eclipse-workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/dalhada/resources/images/bucket/"+filePath);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(image);
			fos.close();
			File newf = new File("C:/unico/eclipse-workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/dalhada/resources/images/bucket/"+filePath);
			/*File f = new File("C:/jjn/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/dalhada/resources/images/bucket/"+filePath);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(image);
			fos.close();
			File newf = new File("C:/jjn/eclipse_workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/dalhada/resources/images/bucket/"+filePath);*/
	    	 if(f.exists())
	    		 f.renameTo(newf);
		}catch (IOException e) {
	    	 e.printStackTrace();
	    	 result = "error";
	    	 
	     }	
		return result;
	}
	@RequestMapping(value="/updatebucket")
	@ResponseBody
	public String updatebucket(HttpSession session, UpdatedBucketVO vo, @RequestParam(value="g_id")String g_id) {	
		String member_id = (String) session.getAttribute("id"), result = "success";
		vo.setMember_id(member_id);
		vo.setGroup_id(Integer.parseInt(g_id));
		if(bucketservice.updateBucket(vo) < 1) result = "error";
		return result;
	}
	@RequestMapping(value="/insertgetbucket")
	@ResponseBody
	public String insertgetbucket(HttpSession session, InsertedBucketVO vo, @RequestParam(value="g_id")String g_id) {	
		String member_id = (String) session.getAttribute("id"), result = "success";
		vo.setMember_id(member_id);
		vo.setGroup_id(Integer.parseInt(g_id));
		if(bucketservice.insertGetBucket(vo) < 1) result = "error";
		return result;
	}
	
	@ModelAttribute("searchBucketVO")
	public SearchBucketVO searchBucketVO() {
		return new SearchBucketVO();
	}
	
	@RequestMapping(value="/searchbucketajax")
	public ModelAndView searchbucketAjax(HttpSession session, @RequestParam(defaultValue="1")int curPage,
			@ModelAttribute("searchBucketVO") SearchBucketVO searchBucketVO) {
		ModelAndView mav = new ModelAndView("jsonView");
		String id = (String) session.getAttribute("id");
		searchBucketVO.setMember_id(id);
		String tagName = searchBucketVO.getSearchTag();
		String keyword = searchBucketVO.getSearchKeyword();
		String action = searchBucketVO.getAction();
		String naverKeyword = null;
		List<NaverBlogVO> naverBlogList = null;
		int listCnt=0;
		PagingVO pageList = null;
		List<BucketVO> searchList = null;
		
		if(tagName != null && action.equals("tag")) {
			listCnt = searchBucketService.getTotalTagCnt(tagName);
			pageList = new PagingVO(listCnt, curPage); //(전체 게시물 수, 현재 페이지)
			searchBucketVO.setStartRow(pageList.getStartIndex());
			searchBucketVO.setEndRow(pageList.getEndIndex());
			mav.addObject("listCnt", listCnt);
			mav.addObject("pagination", pageList);
			
			//태그검색
			searchList = searchBucketService.searchTag(searchBucketVO);
			for(BucketVO vo: searchList) {
				vo.addClass();
			}
			mav.addObject("searchList", searchList);
			mav.addObject("keyword", "");
			
			naverKeyword = (tagName.equals("기타")) ? "버킷리스트": tagName;
			naverBlogList = naverBlogService.selectNaverBlog(naverKeyword,5,1);
			mav.addObject("naverBlogList", naverBlogList);
		}else {
			searchBucketVO.setSearchTag(null);
			//제목검색 검색결과 수&페이징
			listCnt = searchBucketService.getTotalTitleCnt(keyword);
			pageList = new PagingVO(listCnt, curPage); //(전체 게시물 수, 현재 페이지)
			searchBucketVO.setStartRow(pageList.getStartIndex());
			searchBucketVO.setEndRow(pageList.getEndIndex());
			mav.addObject("listCnt", listCnt);
			mav.addObject("pagination", pageList);
			
			//제목검색
			searchList = searchBucketService.searchTitle(searchBucketVO);
			for(BucketVO vo: searchList) {
				vo.addClass();
			}
			mav.addObject("searchList", searchList);
			mav.addObject("keyword", keyword);
			
			naverKeyword = (keyword == null || keyword.equals("") || searchList.isEmpty()) ? "버킷리스트": keyword;
			naverBlogList = naverBlogService.selectNaverBlog(naverKeyword,5,1);
			mav.addObject("naverBlogList", naverBlogList);
		}
		//태그에 해당하는 내용이 없을 경우 메세지 출력
		mav.addObject("tag", tagName);
		
		//관련 동영상 더보기
		mav.addObject("videoKeyword", naverKeyword);
				
		return mav;
	}
    
	@RequestMapping(value="/searchbucket")
	public ModelAndView searchBucket(HttpSession session, @RequestParam(defaultValue="1")int curPage, 
			@ModelAttribute("searchBucketVO") SearchBucketVO searchBucketVO) {
		ModelAndView mav = new ModelAndView();
		String id = (String) session.getAttribute("id");
		searchBucketVO.setMember_id(id);
		String tagName = searchBucketVO.getSearchTag();
		String keyword = searchBucketVO.getSearchKeyword();
		String action = searchBucketVO.getAction();
		String naverKeyword = null;
		List<NaverBlogVO> naverBlogList = null;
		int listCnt = 0;
		PagingVO pageList = null;
		List<BucketVO> searchList = null;
		
		if(tagName != null && action.equals("tag")) {
			listCnt = searchBucketService.getTotalTagCnt(tagName);
			pageList = new PagingVO(listCnt, curPage); //(전체 게시물 수, 현재 페이지)
			searchBucketVO.setStartRow(pageList.getStartIndex());
			searchBucketVO.setEndRow(pageList.getEndIndex());
			mav.addObject("listCnt", listCnt);
			mav.addObject("pagination", pageList);
			
			//태그검색
			searchList = searchBucketService.searchTag(searchBucketVO);
			for(BucketVO vo: searchList) {
				vo.addClass();
			}
			mav.addObject("searchList", searchList);
			mav.addObject("keyword", "");
			
			naverKeyword = (tagName.equals("기타")) ? "버킷리스트": tagName;
			naverBlogList = naverBlogService.selectNaverBlog(naverKeyword,5,1);
			mav.addObject("naverBlogList", naverBlogList);
		}else {
			searchBucketVO.setSearchTag(null);
			//제목검색 검색결과 수&페이징
			listCnt = searchBucketService.getTotalTitleCnt(keyword);
			pageList = new PagingVO(listCnt, curPage); //(전체 게시물 수, 현재 페이지)
			searchBucketVO.setStartRow(pageList.getStartIndex());
			searchBucketVO.setEndRow(pageList.getEndIndex());
			mav.addObject("listCnt", listCnt);
			mav.addObject("pagination", pageList);
			
			//제목검색
			searchList = searchBucketService.searchTitle(searchBucketVO);
			for(BucketVO vo: searchList) {
				vo.addClass();
			}
			mav.addObject("searchList", searchList);
			mav.addObject("keyword", keyword);
			
			naverKeyword = (keyword == null || keyword.equals("") || searchList.isEmpty()) ? "버킷리스트": keyword;
			naverBlogList = naverBlogService.selectNaverBlog(naverKeyword,5,1);
			mav.addObject("naverBlogList", naverBlogList);
		}
		//태그에 해당하는 내용이 없을 경우 메세지 출력
		mav.addObject("tag", tagName);
		
		//태그명 출력
		List<TagInfoVO> tagNameList = searchBucketService.selectTagName();
		mav.addObject("tagNameList", tagNameList);
		
		//관련 동영상 더보기
		mav.addObject("videoKeyword", naverKeyword);
		
		mav.setViewName("searchbucket");
		return mav;
	}
}
