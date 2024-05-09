package PosWeb.POS.service;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 카테고리 페이징 처리
    public Page<Category> findByCtgPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> ctgPaging = categoryRepository.findByCtgItemsPaged(pageable);

        if (ctgPaging.isEmpty())
            return Page.empty();

        return ctgPaging;
    }
}
