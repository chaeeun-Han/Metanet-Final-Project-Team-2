package com.example.myapp.board.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.myapp.board.dao.IBoardRepository;
import com.example.myapp.board.model.Board;
import com.example.myapp.board.model.BoardUploadFile;
import com.example.myapp.board.model.S3Images;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BoardService implements IBoardService {

	@Autowired
	IBoardRepository boardRepository;
	
	// 버킷 이름 동적 할당
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 버킷 주소 동적 할당
    @Value("${cloud.aws.s3.bucket.url}")
    private String defaultUrl;

    @Autowired
    private AmazonS3 s3Client;

    // 파일 업로드
    @Transactional
    public ResponseEntity<Void> uploadFiles(List<MultipartFile> files) {
        try {
            // 업로드된 각 파일 처리
            for (MultipartFile file : files) {
                File fileObj = convertMultiPartFileToFile(file);
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 고유한 파일 이름 생성

                // key가 존재하면 기존 파일은 삭제
                if (s3Client.doesObjectExist(bucketName, fileName)) {
                    s3Client.deleteObject(bucketName, fileName);
                    log.info("중복 제거: {}", fileName);
                }

                // 파일 업로드
                s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
                log.info("S3 업로드 완료: {}", fileName);
                fileObj.delete();
                
                // S3에 저장된 이미지 호출하기
                URL url = s3Client.getUrl(bucketName, fileName);
                String imageUrl = ""+url;

                log.info(imageUrl);
                
                boardRepository.insertS3Images(imageUrl);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    private static File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }
    

	@Transactional
	public void insertArticle(Board board) {
		board.setBoardId(boardRepository.selectMaxArticleNo()+1);
		boardRepository.insertArticle(board);
	}
	
	@Transactional
	public void insertArticle(Board board, BoardUploadFile file) {
		board.setBoardId(boardRepository.selectMaxArticleNo()+1);
		boardRepository.insertArticle(board);
        if(file != null && file.getFileName() != null && !file.getFileName().equals("")) {
        	file.setBoardId(board.getBoardId());
        	file.setFileId(boardRepository.selectMaxFileId()+1);
        	boardRepository.insertFileData(file);
        }
	}

	@Override
	public List<Board> selectArticleListByCategory(int categoryId, int page) {
		int start = (page-1)*10 + 1;
		return boardRepository.selectArticleListByCategory(categoryId, start, start+9); // 오라클은 BETWEEN a AND b에서 a와 b모두 포함하므로 9를 더함
	}

	@Transactional
	public Board selectArticle(int boardId) {
		boardRepository.updateReadCount(boardId);
		return boardRepository.selectArticle(boardId);
	}

	@Override
	public BoardUploadFile getFile(int fileId) {
		return boardRepository.getFile(fileId);
	}
	
	@Transactional
	public void replyArticle(Board board) {
		boardRepository.updateReplyNumber(board.getMasterId(), board.getReplyNumber());
		board.setBoardId(boardRepository.selectMaxArticleNo()+1);
		board.setReplyNumber(board.getReplyNumber()+1);
		board.setReplyStep(board.getReplyStep()+1);
		boardRepository.replyArticle(board);
	}

	@Transactional
	public void replyArticle(Board board, BoardUploadFile file) {
		boardRepository.updateReplyNumber(board.getMasterId(), board.getReplyNumber());
		board.setBoardId(boardRepository.selectMaxArticleNo()+1);
		board.setReplyNumber(board.getReplyNumber()+1);
		board.setReplyStep(board.getReplyStep()+1);
		boardRepository.replyArticle(board);
        if(file != null && file.getFileName() != null && !file.getFileName().equals("")) {
        	file.setBoardId(board.getBoardId());
        	file.setFileId(boardRepository.selectMaxFileId()+1);
        	boardRepository.insertFileData(file);
        }
	}
	
	@Override
	public String getPassword(int boardId) {
		return boardRepository.getPassword(boardId);
	}

	@Override
	public void updateArticle(Board board) {
		boardRepository.updateArticle(board);
	}

	@Transactional
	public void updateArticle(Board board, BoardUploadFile file) {
		boardRepository.updateArticle(board);
        if(file != null && file.getFileName() != null && !file.getFileName().equals("")) {
        	file.setBoardId(board.getBoardId());

        	if(file.getFileId()>0) {
        		boardRepository.updateFileData(file);
        	}else {
        		file.setFileId(boardRepository.selectMaxFileId()+1);
        		boardRepository.insertFileData(file);
        	}
        }
	}
	
	@Override
	public Board selectDeleteArticle(int boardId) {
		return boardRepository.selectDeleteArticle(boardId);
	}
	
	@Transactional
	public void deleteArticle(int boardId, int replyNumber) {
		if(replyNumber>0) {
			boardRepository.deleteReplyFileData(boardId);
			boardRepository.deleteArticleByBoardId(boardId);
		}else if(replyNumber==0){
			boardRepository.deleteFileData(boardId);
			boardRepository.deleteArticleByMasterId(boardId);
		}else {
			throw new RuntimeException("WRONG_REPLYNUMBER");
		}
	}

	@Override
	public int selectTotalArticleCount() {
		return boardRepository.selectTotalArticleCount();
	}

	@Override
	public int selectTotalArticleCountByCategoryId(int categoryId) {
		return boardRepository.selectTotalArticleCountByCategoryId(categoryId);
	}

	@Override
	public List<Board> searchListByContentKeyword(String keyword, int page) {
		int start = (page-1)*10 + 1;
		return boardRepository.searchListByContentKeyword("%"+keyword+"%", start, start+9); // 오라클은 BETWEEN a AND b에서 a와 b모두 포함하므로 9를 더함
	}

	@Override
	public int selectTotalArticleCountByKeyword(String keyword) {
		return boardRepository.selectTotalArticleCountByKeyword("%"+keyword+"%");
	}

}