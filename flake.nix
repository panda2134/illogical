{
  description = "illogical";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }: 
    flake-utils.lib.eachDefaultSystem ( system:
      let pkgs = nixpkgs.legacyPackages.${system};
      in {
        devShell = pkgs.mkShell {
          shellHook = ''
            echo "Welcome to dev shell!"
            echo "run \`codium .\` to start developing"
            export DONT_PROMPT_WSL_INSTALL=1
          '';
          packages = [
            pkgs.scala
            pkgs.sbt
            (pkgs.vscode-with-extensions.override {
              vscode = pkgs.vscodium;
              vscodeExtensions = with pkgs.vscode-extensions; [
                scala-lang.scala
                scalameta.metals
                bbenoist.nix
              ];
            })
          ];
        };
      }
    );
}
