package PosWeb.POS.service;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.domain.dto.Item.StoreItemForm;
import PosWeb.POS.domain.dto.Item.AddItemForm;
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
        // 상품 중복성 검사
        if (itemRepository.findByName(storeItemForm.getName()) != null) {
            return "Fail";
        }

        Item newItem = new Item();
        Optional<Category> ctg = findCategory(storeItemForm.getCategory());  // 카테고리가 데이터베이스에 있는지 검사 없으면 새로운 카테고리 생성
        ctg.get().addItem();    // 해당 카테고리의 총 상품 개수를 1개 증가

        // 상품 생성
        newItem.setName(storeItemForm.getName());
        newItem.setPrice(storeItemForm.getPrice());
        newItem.setCategory(ctg.get());  // 상품에 Category set
        newItem.setCompany(storeItemForm.getCompany());

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

    // 상품 입고
    @Transactional
    public void addItem(AddItemForm AddItemForm) {
        Item findItem = itemRepository.findOne(AddItemForm.getId());
        System.out.println("여기까지 출력 됨?---1");
        findItem.setStockQuantity(AddItemForm.getStockQuantity());
        System.out.println("여기서 문제가 생기는거 같은뎅....");
        System.out.println(findItem.getStockQuantity());
    }

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
        Category ctg = findCategory(category).get();
        return itemRepository.findByCtgItemsPaged(ctg, pageable);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(String name) {
        return itemRepository.findByName(name);
    }
    public Item findById(int id) { return itemRepository.findOne((long) id);}
}
