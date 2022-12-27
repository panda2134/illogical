{
  description = "illogical";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    scala-neovim.url = "github:gvolpe/neovim-flake";
  };

  outputs = { self, nixpkgs, flake-utils, scala-neovim }: 
    flake-utils.lib.eachDefaultSystem ( system:
      let pkgs = nixpkgs.legacyPackages.${system};
      in {
        devShell = pkgs.mkShell {
          packages = [
            pkgs.scala
            pkgs.sbt
            # scala-neovim.packages.${system}.default
          ];
        };
      }
    );
}
