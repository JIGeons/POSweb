package PosWeb.POS.service;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.repository.CategoryRepository;
import PosWeb.POS.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void saveItem(Item item, String category) {
        Optional<Category> ctg = findCategory(category);  // 카테고리가 데이터베이스에 있는지 검사 없으면 새로운 카테고리 생성
        item.setCategory(ctg.get());  // 상품에 Category set
        itemRepository.save(item);  // 저장
    }

    @Transactional
    public void updateItem(Long itemId, String name, String category, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);
        Optional<Category> ctg = findCategory(category);
        findItem.setName(name);
        findItem.setCategory(ctg.get());
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
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

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(String name) {
        return itemRepository.findByName(name);
    }
}
