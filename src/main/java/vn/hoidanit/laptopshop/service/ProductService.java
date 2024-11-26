package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailRepository cartDetailRepository, UserService userService, OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Product createProduct(Product pr) {
        return this.productRepository.save(pr);
    }

    public Page<Product> fetchProducts(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    public Optional<Product> fetchProductById(long id) {
        return this.productRepository.findById(id);
    }

    public void deleteProduct(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {
        // get logged user by email
        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            // check if user has cart
            Cart cart = this.cartRepository.findByUser(user);
            if (cart == null) {
                // create new cart
                cart = new Cart();
                cart.setUser(user);
                cart.setSum(0);
                this.cartRepository.save(cart);
            }

            // already has cart, add product to cart
            var optionalProduct = this.productRepository.findById(productId);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();

                // check if product already in cart
                CartDetail cartDetail = this.cartDetailRepository.findByCartAndProduct(cart, product);
                if (cartDetail == null) {
                    // add new product to cart
                    cartDetail = new CartDetail();
                    cartDetail.setCart(cart);
                    cartDetail.setProduct(product);
                    cartDetail.setPrice(product.getPrice());
                    cartDetail.setQuantity(quantity);
                    this.cartDetailRepository.save(cartDetail);

                    // update cart sum
                    int sum = cart.getSum() + 1;
                    cart.setSum(sum);
                    this.cartRepository.save(cart);
                    session.setAttribute("sum", sum);
                } else {
                    // increase quantity of product in cart
                    cartDetail.setQuantity(cartDetail.getQuantity() + quantity);
                    this.cartDetailRepository.save(cartDetail);
                }
            }
        }
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handleRemoveCartDetail(long cartDetailId, HttpSession session) {
        Optional<CartDetail> optionalCartDetail = this.cartDetailRepository.findById(cartDetailId);
        if (optionalCartDetail.isPresent()) {
            CartDetail cartDetail = optionalCartDetail.get();
            Cart cart = cartDetail.getCart();
            this.cartDetailRepository.deleteById(cartDetailId);

            if (cart.getSum() > 1) {
                int newSum = cart.getSum() - 1;
                cart.setSum(newSum);
                session.setAttribute("sum", newSum);
                this.cartRepository.save(cart);
            } else {
                this.cartRepository.delete(cart);
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for (CartDetail cartDetail : cartDetails) {
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if (cdOptional.isPresent()) {
                CartDetail currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currentCartDetail);
            }
        }
    }

    public void handlePlaceOrder(User user, HttpSession session, String receiverName, String receiverAddress,
            String receiverPhone) {
        // get cart details
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();
            if (cartDetails != null) {
                // create new order
                Order order = new Order();
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("PENDING");

                double total = 0;
                for (CartDetail cartDetail : cartDetails) {
                    total += cartDetail.getPrice() * cartDetail.getQuantity();
                }
                order.setTotalPrice(total);
                order = this.orderRepository.save(order);

                // create order details
                for (CartDetail cartDetail : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(cartDetail.getProduct());
                    orderDetail.setPrice(cartDetail.getPrice());
                    orderDetail.setQuantity(cartDetail.getQuantity());
                    this.orderDetailRepository.save(orderDetail);
                }

                // delete cart and cart details
                this.cartDetailRepository.deleteAll(cartDetails);
                this.cartRepository.delete(cart);

                // reset session sum
                session.setAttribute("sum", 0);
            }
        }
    }
}
