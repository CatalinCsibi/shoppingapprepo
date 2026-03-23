package com.demo.shoppingapp.service.impl;

import com.demo.shoppingapp.dto.request.CreateProductRequest;
import com.demo.shoppingapp.dto.request.ProductSearchRequest;
import com.demo.shoppingapp.dto.response.PagedResponse;
import com.demo.shoppingapp.dto.response.ProductResponse;
import com.demo.shoppingapp.exception.BusinessException;
import com.demo.shoppingapp.exception.ResourceNotFoundException;
import com.demo.shoppingapp.mapper.ProductMapper;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.demo.shoppingapp.support.TestDataFactory.createProductRequest;
import static com.demo.shoppingapp.support.TestDataFactory.product;
import static com.demo.shoppingapp.support.TestDataFactory.productResponse;
import static com.demo.shoppingapp.support.TestDataFactory.productSearchRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void searchProductsBuildsTheRequestedPageableAndMapsResults() {
        ProductSearchRequest request = productSearchRequest();
        Product product = product();
        ProductResponse response = productResponse(product.getId());
        Page<Product> page = new PageImpl<>(
                List.of(product),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name")),
                1
        );

        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(response);

        PagedResponse<ProductResponse> result = productService.searchProducts(request);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("name")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(result.content()).containsExactly(response);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void getProductByIdReturnsMappedProduct() {
        Product product = product();
        ProductResponse response = productResponse(product.getId());

        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getProductById(product.getId());

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getProductByIdThrowsWhenActiveProductIsMissing() {
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void createProductRejectsDuplicateNames() {
        CreateProductRequest request = createProductRequest();
        when(productRepository.existsByNameIgnoreCase(request.name())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProductMapsAndSavesANewProduct() {
        CreateProductRequest request = createProductRequest();
        Product product = product();
        ProductResponse response = productResponse(product.getId());

        when(productRepository.existsByNameIgnoreCase(request.name())).thenReturn(false);
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.createProduct(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void updateProductLoadsExistingEntityAndSavesChanges() {
        UUID id = UUID.randomUUID();
        CreateProductRequest request = createProductRequest();
        Product product = product(id, "Old name", 5);
        ProductResponse response = productResponse(id);

        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(product));
        doAnswer(invocation -> {
            CreateProductRequest update = invocation.getArgument(0);
            Product target = invocation.getArgument(1);
            target.setName(update.name());
            target.setCategory(update.category());
            return null;
        }).when(productMapper).updateEntityFromRequest(eq(request), any(Product.class));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.updateProduct(id, request);

        assertThat(product.getName()).isEqualTo(request.name());
        assertThat(product.getCategory()).isEqualTo(request.category());
        assertThat(result).isEqualTo(response);
    }

    @Test
    void deleteProductSoftDeletesTheEntity() {
        UUID id = UUID.randomUUID();
        Product product = product(id, "Phone", 10);
        when(productRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.deleteProduct(id);

        assertThat(product.getActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void getAllCategoriesDelegatesToTheRepository() {
        when(productRepository.findAllCategories()).thenReturn(List.of("phones", "audio"));

        List<String> result = productService.getAllCategories();

        assertThat(result).containsExactly("phones", "audio");
    }
}
