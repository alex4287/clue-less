package clueless;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
class GameController {

	private final GameRepository gameRepository;
	private final GameModelAssembler gameAssembler;

	GameController(GameRepository gameRepository, GameModelAssembler gameAssembler) {
		this.gameRepository = gameRepository;
		this.gameAssembler = gameAssembler;
	}

	/**
	 * Returns list of all active games
	 * @return
	 */
	@GetMapping()
	CollectionModel<EntityModel<Game>> all() {
		List<EntityModel<Game>> games = gameRepository.findAll().stream()
				.map(game -> EntityModel.of(game,
						linkTo(methodOn(GameController.class).one(game.getId())).withSelfRel(),
						linkTo(methodOn(GameController.class).all()).withRel("games")))
				.collect(Collectors.toList());

		return CollectionModel.of(games, linkTo(methodOn(GameController.class).all()).withSelfRel());
	}

	/**
	 * Creates a new game object (starts new game)
	 * @param newGame
	 * @return
	 */
	@PostMapping()
	ResponseEntity<?> newGame(@RequestBody Game newGame) {

		EntityModel<Game> entityModel = gameAssembler.toModel(gameRepository.save(newGame));

		return ResponseEntity //
				.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
				.body(entityModel);
	}

	/**
	 * Returns the specific game object requested
	 * @param gid
	 * @return
	 */
	@GetMapping("/{gid}")
	EntityModel<Game> one(@PathVariable Long gid) {

		Game game = gameRepository.findById(gid) //
				.orElseThrow(() -> new GameNotFoundException(gid));

		return EntityModel.of(game, //
				linkTo(methodOn(GameController.class).one(gid)).withSelfRel(),
				linkTo(methodOn(GameController.class).all()).withRel("games"));
	}

	/**
	 * Deletes the provided game
	 * @param gid
	 */
	@DeleteMapping("/{gid}")
	void deleteGame(@PathVariable Long gid) {
		gameRepository.deleteById(gid);
	}
	
	// TODO: fix this to get all players in game
//	/**
//	 * Returns list of players for a given game
//	 * @return
//	 */
//	@GetMapping("/games/{gid}/players")
//	CollectionModel<Player> allPlayersInGame(@PathVariable Long gid) {
//		
//		Game game = gameRepository.findById(gid) //
//				.orElseThrow(() -> new GameNotFoundException(gid));
//		
//		ArrayList<Player> currPlayers = game.getPlayers();
//		
//		return CollectionModel.of(currPlayers);
//	}
	
}