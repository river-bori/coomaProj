package coo.user.control;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import coo.user.db.HmFileData;
import coo.user.db.HmDogsDTO;
import coo.user.db.HmLoginMapper;
import coo.user.db.HmMemberDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HmLoginController {
	@Resource
	HmLoginMapper lm;
	//회원가입///////////////////////////////////////
	@GetMapping("/user/log/joinForm")
	String joinForm(HttpSession session, Model mm,HmMemberDTO dto, HmDogsDTO dog) {
		String pid = (String)session.getAttribute("pid");
		mm.addAttribute("pid", pid);
		return "user/log/joinForm";
	}
	
	@PostMapping("/user/log/joinForm")
	String joinComplete(HttpServletRequest request, Model mm, HmDogsDTO dog, HmMemberDTO dto, HmFileData fd) {
		System.out.println("회원가입 진입");
	    if(fd.getDogimg().getContentType().startsWith("image/")) {
	    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		    try {
		        Date birthDate = format.parse(fd.getBirthstr());
		        dto.setBirth(birthDate);
		    } catch (ParseException e) {
		        // 날짜 변환 실패 시
		        e.printStackTrace();
		    }
		    String res = fileSave(fd.getDogimg(), request);
	        dog.setPhoto(res);
	        dto.setEmail(dto.getEmail()+"@"+fd.getEmail2());
			dto.setPwanswer(dto.getPwanswer().trim());
			dto.setAddr2(dto.getAddr2().trim());
			dog.setDname(dog.getDname().trim());
			dog.setBreed(dog.getBreed().trim());
		    dto.setDog1(dog.getDname());
		    lm.insert(dto);
		    lm.insertDog(dog);
		} 
		
		else {
			mm.addAttribute("msg","파일은 이미지 파일만 업로드할 수 있습니다.");
			mm.addAttribute("goUrl","/user/log/joinForm");
			return "user/log/alert";
		}
	    
		mm.addAttribute("msg","회원가입이 완료되었습니다.");
		mm.addAttribute("goUrl","/user/log/login");
		return "user/log/alert";
	}
	
	//로그인///////////////////////////////////////
	@GetMapping("/user/log/login")
	String login(HttpSession session, Model mm,HmMemberDTO dto) {
		String pid = (String)session.getAttribute("pid");
		mm.addAttribute("pid", pid);
		return "user/log/login";
	}
	
	@PostMapping("/user/log/login")
	String logincomplete(HmMemberDTO dto, HttpSession session, Model mm) {
		System.out.println("로그인창");
		//int cnt = lm.logchk(dto);
		HmMemberDTO memData = lm.logchk(dto);
		
		if(memData.getGrade().equals("블랙")) {
			mm.addAttribute("msg","블랙리스트 입니다.");
			mm.addAttribute("goUrl","/user/log/login");
		}
		else if(lm.logchk(dto)!=null) {
			mm.addAttribute("msg",memData.getPname()+"님 로그인 되었습니다.");
			mm.addAttribute("goUrl","/user");
			if(memData.getGrade().equals("관리자")) {
				mm.addAttribute("goUrl","/admin");
			}
			session.setAttribute("pid", memData.getPid());
			session.setAttribute("pname", memData.getPname());
		}
		else {
			mm.addAttribute("msg","아이디 또는 비밀번호를 잘못 입력하였습니다.");
			mm.addAttribute("goUrl","/user/log/login");
		}
		return "user/log/alert";
	}
	
	//로그아웃///////////////////////////////////////
	@RequestMapping("/user/log/logout")
	String logout(HttpSession session, Model mm) {
		Object pname = session.getAttribute("pname");
		
		session.invalidate();
		
		mm.addAttribute("msg",pname+"님 로그아웃되었습니다.");
		mm.addAttribute("goUrl","/user");
		
		return "user/log/alert";
	}
	
	//사진저장///////////////////////////////////////
    String fileSave(MultipartFile mf,    HttpServletRequest request) {
        String path = request.getServletContext().getRealPath("dimg")+"/";
        //System.out.println(path);
        String res = mf.getOriginalFilename();
        File ff = new File(path+res);
        int pos = res.lastIndexOf(".");
        String fName = res.substring(0,pos);
        String ext = res.substring(pos);
        int  no = 0;
        while(ff.exists()) {
            no++;
            res = fName+no+ext;
            ff = new File(path+res);
        }
        try {
            FileOutputStream fos = new FileOutputStream(ff);
            fos.write(mf.getBytes());
            fos.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return res;
    }
	
	@RequestMapping("/user/checkId.do")
	@ResponseBody
    public String checkId(@RequestParam("pid") String pid) {
        String result="N";
        
        if(pid.isEmpty()) {
            return "F"; // "F"를 반환하여 사용 불가능한 아이디임을 알립니다.
        }
        else {
            int flag = lm.checkId(pid);
            if(flag == 1) {
            	result="Y"; // "Y"를 반환하여 사용 가능한 아이디임을 알립니다.
            }
            else {
            	result="N"; // "N"을 반환하여 사용 불가능한 아이디임을 알립니다.
            }
        }
        //아이디가 있을시 Y 없을시 N 으로jsp view 로 보냄
        return result;
    }
	
	//전화번호 중복검사
	@RequestMapping("/user/checkTel.do")
	@ResponseBody
    public String checkTel(@RequestParam("tel") String tel) {
		System.out.println("전화번호중복검사");
        String result="NN";
        
        if(tel.isEmpty()) {
            return "FF"; // "F"를 반환하여 사용 불가능한 아이디임을 알립니다.
        }
        else {
            int flag = lm.checkTel(tel);
            if(flag == 1) {
            	result="YY"; // "Y"를 반환하여 사용 가능한 아이디임을 알립니다.
            }
            else {
            	result="NN"; // "N"을 반환하여 사용 불가능한 아이디임을 알립니다.
            }
        }
        System.out.println("전화번호중복검사 끝");
        //아이디가 있을시 Y 없을시 N 으로jsp view 로 보냄
        return result;
    }
	
	//아이디찾기///////////////////////////////////////
		@GetMapping("/user/log/findId")
		String findId(Model mm, HmMemberDTO dto) {
			
			return "user/log/findId";
		}
		
		@PostMapping("/user/log/findId")
		String findIdComplete(Model mm, HmMemberDTO dto) {
			
			HmMemberDTO myData = lm.findId(dto);
			
			String msg = "이름 또는 전화번호가 일치하지 않습니다.";
			String goUrl = "/user/log/findId";
			
			if(myData!=null) {
				msg = "아이디: "+myData.getPid();
				goUrl = "/user/log/login";
			}
			
			mm.addAttribute("msg", msg);
			mm.addAttribute("goUrl", goUrl);
			
			return "user/log/alert";
		}
		
		//비번찾기///////////////////////////////////////
		@GetMapping("/user/log/findPw")
		String findPw(Model mm, HmMemberDTO dto) {
			
			return "user/log/findPw";
		}
		
		@PostMapping("/user/log/findPw")
		String findPwComplete(Model mm, HmMemberDTO dto) {
			
			int cnt = lm.findPw(dto);
			
			if(cnt==0) {
				mm.addAttribute("msg", "아이디 또는 비밀번호 확인이 일치하지 않습니다.");
				mm.addAttribute("goUrl", "/user/log/findPw");
				return "user/log/alert";
			}
			
			return  "/user/log/rePw";
		}
		
		@PostMapping("/user/log/rePw")
		String rePwComplete(Model mm, HmMemberDTO dto) {
			System.out.println("rePw진입 ㅡ pid="+dto.getPid());
			
			lm.rePw(dto);
			
			mm.addAttribute("msg", "비밀번호가 변경되었습니다.");
			mm.addAttribute("goUrl", "/user");
			
			return  "/user/log/alert";
		}
		
		@RequestMapping("/user/log/adminSelect")
		String adselcet(HttpSession session, Model mm) {
			String pid = (String)session.getAttribute("pid");
			mm.addAttribute("pid", pid);
			
			return "user/log/adminSelect";
		}
}
