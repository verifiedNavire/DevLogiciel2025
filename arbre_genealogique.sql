-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : ven. 23 mai 2025 à 00:46
-- Version du serveur : 8.2.0
-- Version de PHP : 8.2.13

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `arbre_genealogique`
--

-- --------------------------------------------------------

--
-- Structure de la table `arbre`
--

DROP TABLE IF EXISTS `arbre`;
CREATE TABLE IF NOT EXISTS `arbre` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_utilisateur` int DEFAULT NULL,
  `visibilite` enum('prive','public','protege') DEFAULT 'prive',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `arbre`
--

INSERT INTO `arbre` (`id`, `id_utilisateur`, `visibilite`) VALUES
(6, 6, 'prive');

-- --------------------------------------------------------

--
-- Structure de la table `lien_parent`
--

DROP TABLE IF EXISTS `lien_parent`;
CREATE TABLE IF NOT EXISTS `lien_parent` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_enfant` int NOT NULL,
  `id_parent` int NOT NULL,
  `type_lien` enum('pere','mere') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_lien` (`id_enfant`,`type_lien`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `personne`
--

DROP TABLE IF EXISTS `personne`;
CREATE TABLE IF NOT EXISTS `personne` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `date_naissance` date DEFAULT NULL,
  `mot_de_passe` varchar(255) DEFAULT NULL,
  `inscrit` tinyint(1) DEFAULT '1',
  `photo` varchar(255) DEFAULT 'defaut.png',
  `niveau` int DEFAULT NULL,
  `id_arbre` int DEFAULT NULL,
  `securite_sociale` varchar(50) DEFAULT NULL,
  `nationalite` varchar(100) DEFAULT NULL,
  `fichier_identite` varchar(255) DEFAULT NULL,
  `visibilite` enum('public','private') DEFAULT 'private',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `personne`
--

INSERT INTO `personne` (`id`, `nom`, `prenom`, `date_naissance`, `mot_de_passe`, `inscrit`, `photo`, `niveau`, `id_arbre`, `securite_sociale`, `nationalite`, `fichier_identite`, `visibilite`) VALUES
(8, 'saidi', 'besma', '2004-08-09', 'fds', 1, 'defaut.png', 0, 6, '234567', 'FR', NULL, 'private'),
(0, 'admin', 'admin', '2000-09-09', '123', 1, 'defaut.png', 0, 3, '', '', NULL, 'private');

-- --------------------------------------------------------

--
-- Structure de la table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
CREATE TABLE IF NOT EXISTS `utilisateur` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(191) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `prenom` varchar(100) DEFAULT NULL,
  `id_personne` int DEFAULT NULL,
  `code_public` varchar(20) DEFAULT NULL,
  `code_prive` varchar(20) DEFAULT NULL,
  `statut` enum('en_attente','valide','refuse') DEFAULT 'en_attente',
  `role` enum('utilisateur','admin') DEFAULT 'utilisateur',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `utilisateur`
--

INSERT INTO `utilisateur` (`id`, `email`, `mot_de_passe`, `nom`, `prenom`, `id_personne`, `code_public`, `code_prive`, `statut`, `role`) VALUES
(6, 'saidibesma@maildrop.cc', '123', 'saidi', 'besma', 8, 'PUB-4C61A8', 'PRV-7EA3E4', 'valide', 'utilisateur'),
(0, 'admin@mail.com', '0000', 'admin', 'admin', 3, 'PUB-5E837B', '0000', 'valide', 'admin');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
