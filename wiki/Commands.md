LWC's comamnd system allows you to do almost everything you would need to from either in-game or the command line.

* Arguments surrounded with &lt;&gt; are required
* Arguments surrounded with [] are optional
* Arguments such as <NAME> or [NAME] must be replaced by a player's name

## Protection Commands
<table width="100%">
    <tr>
        <th width="30%">Command</td>
        <th width="15%">Alias(es)</td>
        <th width="15%">Permission</td>
        <th>Description</td>
    </tr>

    <tr>
        <td> lwc create </td>
        <td> <code>cprivate</code> <code>clock</code> </td>
        <td> lwc.create </td>
        <td> Creates a protection that only you can access. You can give others access to the protection by using the `lwc add` command </td>
    </tr>

    <tr>
        <td> lwc delete </td>
        <td> <code>cremove</code> <code>cunlock</code> </td>
        <td> lwc.delete </td>
        <td> Removes a protection that you have <code>OWNER</code> access to </td>
    </tr>

    <tr>
        <td> lwc info </td>
        <td> <code>cinfo</code> </td>
        <td> lwc.info </td>
        <td> Views information about a protection </td>
    </tr>
</table>

## Attribute commands
<table width="100%">
    <tr>
        <th width="30%">Command</td>
        <th width="15%">Alias(es)</td>
        <th width="15%">Permission</td>
        <th>Description</td>
    </tr>

    <tr>
        <td> lwc set &lt;ATTRIBUTE&gt; [VALUE] </td>
        <td> <code>cset</code> </td>
        <td> lwc.attribute.set </td>
        <td> Sets an attribute ATTRIBUTE onto a protection. More info about what attributes you can use can be found at [[Attributes]] </td>
    </tr>

    <tr>
        <td> lwc unset &lt;ATTRIBUTE&gt; </td>
        <td> <code>cunset</code> </td>
        <td> lwc.attribute.delete </td>
        <td> Removes an attribute ATTRIBUTE from a protection. More info about what attributes you can use can be found at [[Attributes]] </td>
    </tr>
</table>