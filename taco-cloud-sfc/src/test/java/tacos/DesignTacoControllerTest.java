package tacos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tacos.Ingredient.Type;
import tacos.data.IngredientRepository;
import tacos.data.OrderRepository;
import tacos.data.TacoRepository;
import tacos.data.UserRepository;
import tacos.web.DesignTacoController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DesignTacoController.class)
public class DesignTacoControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	private List<Ingredient> ingredients;
	
	private Taco design;
	
	@MockBean
	private IngredientRepository ingredientRepository;
	
	@MockBean
	private TacoRepository designRepository;
	
	@MockBean
	private OrderRepository orderRepository;
	
	@MockBean
	private UserRepository userRepository;
	
	@BeforeEach
	public void setup() {
		ingredients = Arrays.asList(
				new Ingredient("FLTO", "Flour Tortilla", Type.WRAP),
				new Ingredient("COTO", "Corn Tortilla", Type.WRAP),
				new Ingredient("GRBF", "Ground Beef", Type.PROTEIN),
				new Ingredient("CARN", "Carnitas", Type.PROTEIN),
				new Ingredient("TMTO", "Diced Tomatoes", Type.VEGGIES),
				new Ingredient("LETC", "Lettuce", Type.VEGGIES),
				new Ingredient("CHED", "Cheddar", Type.CHEESE),
				new Ingredient("JACK", "Monterrey Jack", Type.CHEESE),
				new Ingredient("SLSA", "Salsa", Type.SAUCE),
				new Ingredient("SRCR", "Sour Cream", Type.SAUCE)
		);
		
		when(ingredientRepository.findAll())
				.thenReturn(ingredients);
		
		when(ingredientRepository.findById("FLTO")).thenReturn(Optional.of(new Ingredient("FLTO", "Flour Tortilla", Type.WRAP)));
		when(ingredientRepository.findById("GRBF")).thenReturn(Optional.of(new Ingredient("GRBF", "Ground Beef", Type.PROTEIN)));
		when(ingredientRepository.findById("CHED")).thenReturn(Optional.of(new Ingredient("CHED", "Cheddar", Type.CHEESE)));
		
		design = new Taco();
		design.setName("Test Taco");
		
		design.setIngredients(Arrays.asList(
				new Ingredient("FLTO", "Flour Tortilla", Type.WRAP),
				new Ingredient("GRBF", "Ground Beef", Type.PROTEIN),
				new Ingredient("CHED", "Cheddar", Type.CHEESE)
		));
		
		when(userRepository.findByUsername("testuser"))
				.thenReturn(new User("testuser", "testpass", "Test User", "123 Street", "Someville", "CO", "12345", "123-123-1234"));
	}
	
	@Test
	@WithMockUser(username = "testuser", password = "testpass")
	public void testShowDesignForm() throws Exception {
		mockMvc.perform(get("/design"))
				.andExpect(status().isOk())
				.andExpect(view().name("design"))
				.andExpect(model().attribute("wrap", ingredients.subList(0, 2)))
				.andExpect(model().attribute("protein", ingredients.subList(2, 4)))
				.andExpect(model().attribute("veggies", ingredients.subList(4, 6)))
				.andExpect(model().attribute("cheese", ingredients.subList(6, 8)))
				.andExpect(model().attribute("sauce", ingredients.subList(8, 10)));
	}
	
	@Test
	@WithMockUser(username = "testuser", password = "testpass", authorities = "ROLE_USER")
	public void processTaco() throws Exception {
		when(designRepository.save(design))
				.thenReturn(design);
		
		mockMvc.perform(post("/design").with(csrf())
						.content("name=Test+Taco&ingredients=FLTO,GRBF,CHED")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().stringValues("Location", "/orders/current"));
	}
	
}
