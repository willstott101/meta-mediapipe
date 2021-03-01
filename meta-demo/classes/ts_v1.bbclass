inherit unsupportarch tensorflow_ver

python __anonymous() {
    if '--config=v1' not in (d.getVar("TF_ARGS_EXTRA") or "").split():
        msg =  "\nIt requires tensorflow 1.x, add 'TF_ARGS_EXTRA = \"--config=v1\"' to local.conf"
        raise bb.parse.SkipPackage(msg)
}
