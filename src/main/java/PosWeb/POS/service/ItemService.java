package PosWeb.POS.service;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.domain.dto.Item.AddItemForm;
import PosWeb.POS.domain.dto.Item.StoreItemForm;
import PosWeb.POS.repository.CategoryRepository;
import PosWeb.POS.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public String saveItem(StoreItemForm storeItemForm) {
        Item findItem = itemRepository.findByName(storeItemForm.getName());
        // 상품 중복성 검사
        if (findItem != null) {
            // 상품 상태가 비활성인지 확인
            if (findItem.isDisable()) { // 비활성 상태인 경우
                findItem.setDisable(!findItem.isDisable()); // 상품을 활성화 시킨다.
                findItem.getCategory().addItem();   // 카테고리의 총 갯수를 증가시킨다.
                return "Success";
            }
            return "Fail";
        }

        Item newItem = new Item();
        // 카테고리가 데이터베이스에 있는지 검사 없으면 새로운 카테고리 생성
        Optional<Category> ctg = findCategory(storeItemForm.getCategory());
        // 해당 카테고리의 총 상품 개수를 1개 증가
        ctg.get().addItem();

        // 상품 생성
        newItem.setName(storeItemForm.getName());
        newItem.setPrice(storeItemForm.getPrice());
        newItem.setCategory(ctg.get());  // 상품에 Category set
        newItem.setCompany(storeItemForm.getCompany());
        newItem.setDisable(false);

        itemRepository.save(newItem);  // 저장
        return "Success";
    }

    // 상품 정보 수정
    @Transactional
    public void updateItem(Long itemId, String name, String category, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);
        Optional<Category> ctg = findCategory(category);
        findItem.setName(name);
        findItem.setCategory(ctg.get());
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
    }

    // 상품 삭제(상품 비활성화)
    @Transactional
    public void deleteItem(String itemName) {
        Item findItem = itemRepository.findByName(itemName);
        findItem.setDisable(true);              // 상품 비활성화 버튼 true로 set
        findItem.setStockQuantity(0);           // 상품 재고를 0으로 설정
        findItem.getCategory().removeItem();    // 카테고리 상품 총 갯수를 하나 줄인다.
    }

    // 상품 입고
    @Transactional
    public void addItem(AddItemForm AddItemForm) {
        Item findItem = itemRepository.findOne(AddItemForm.getId());
        findItem.setStockQuantity(findItem.getStockQuantity() + AddItemForm.getStockQuantity());
    }

    // 카테고리 검사 만약 카테고리가 없으면 생성
    private Optional<Category> findCategory(String content) {
        Optional<Category> category = categoryRepository.findByCategory(content);

        // 카테고리 목록에 있는 카테고리가 아닐 시
        if (category.isEmpty()) {
            Category ctg = new Category();
            ctg.setCategory(content);
            categoryRepository.save(ctg);  // 새로운 카테고리를 생성하고 저장한다.
            category = categoryRepository.findByCategory(ctg.getCategory());
        }

        return category;
    }

    public List<Item> findCategoryItems(Category category) {
        List<Item> items = new ArrayList<>();
        return items = itemRepository.findByCategory(category);
    }

    public Page<Item> findByCtgItemsPaged(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Optional<Category> optionalCategory = findCategory(category);
        // 카테고리가 있는지 없는지 검사
        if (optionalCategory.isPresent()) { // 카테고리가 있는경우
            Category ctg = optionalCategory.get();
            return itemRepository.findByCtgItemsPaged(ctg, pageable);
        } else {    // 카테고리가 없는 경우 빈페이지 반환
            return Page.empty();
        }
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(String name) {
        return itemRepository.findByName(name);
    }

    public Item findOne(Long id) {
        return itemRepository.findOne(id);
    }
    public Item findById(int id) { return itemRepository.findOne((long) id);}
}
