package PosWeb.POS.service;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.repository.CategoryRepository;
import PosWeb.POS.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, String name, String category, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);
        Category ctg = findCategory(category);
        findItem.setName(name);
        findItem.setCategory(ctg);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
    }

    private Category findCategory(String content) {
        Category category = categoryRepository.findByCategory(content);

        // 카테고리 목록에 있는 카테고리가 아닐 시
        if (category == null) {
            category = new Category();
            category.setCategory(content);
            categoryRepository.save(category);  // 새로운 카테고리를 생성하고 저장한다.
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
